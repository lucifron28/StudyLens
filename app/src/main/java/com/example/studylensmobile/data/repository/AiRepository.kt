package com.example.studylensmobile.data.repository

import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.core.format.toReadableDate
import com.example.studylensmobile.data.remote.apiResult
import com.example.studylensmobile.data.remote.api.AiApi
import com.example.studylensmobile.data.remote.dto.FlashcardDto
import com.example.studylensmobile.data.remote.dto.GenerateFlashcardsRequest
import com.example.studylensmobile.data.remote.dto.GenerateQuizRequest
import com.example.studylensmobile.data.remote.dto.QuizDto
import com.example.studylensmobile.data.remote.dto.QuizQuestionDto
import com.example.studylensmobile.data.remote.dto.StartTutorRequest
import com.example.studylensmobile.data.remote.dto.SummaryDto
import com.example.studylensmobile.data.remote.dto.SummaryRequest
import com.example.studylensmobile.data.remote.dto.TutorMessageDto
import com.example.studylensmobile.data.remote.dto.TutorMessageRequest
import com.example.studylensmobile.data.remote.dto.TutorResponseDto
import com.example.studylensmobile.data.remote.dto.TutorSessionDto
import com.example.studylensmobile.domain.model.Flashcard
import com.example.studylensmobile.domain.model.Quiz
import com.example.studylensmobile.domain.model.QuizQuestion
import com.example.studylensmobile.domain.model.Summary
import com.example.studylensmobile.domain.model.TutorMessage
import com.example.studylensmobile.domain.model.TutorSession

class AiRepository(
    private val aiApi: AiApi
) : AiCacheInvalidator {
    private val summaryCache = mutableMapOf<AiCacheKey, Summary>()
    private val flashcardsCache = mutableMapOf<AiCacheKey, List<Flashcard>>()
    private val quizCache = mutableMapOf<AiCacheKey, Quiz>()
    private val tutorConversationCache = mutableMapOf<AiCacheKey, TutorConversation>()
    private val tutorSessionCacheKeys = mutableMapOf<String, AiCacheKey>()

    suspend fun generateSummary(
        sourceType: String,
        sourceId: String,
        forceRefresh: Boolean = false
    ): Result<Summary> {
        val source = sourceFor(sourceType, sourceId)
            ?: return Result.failure(Exception("Unsupported or invalid summary source."))
        val cacheKey = AiCacheKey(sourceType = sourceType, sourceId = sourceId)
        if (!forceRefresh) {
            summaryCache[cacheKey]?.let { return Result.success(it) }
        }

        val request = SummaryRequest(
            moduleId = source.moduleId,
            chapterId = source.chapterId,
            boardScanId = source.boardScanId
        )

        return apiResult("Summary", { aiApi.summarize(request) }) {
            it.toDomain()
                .also { summary -> summaryCache[cacheKey] = summary }
        }.withAiFailureMessage("Summary generation")
    }

    suspend fun generateFlashcards(
        sourceType: String,
        sourceId: String,
        count: Int = 5,
        forceRefresh: Boolean = false
    ): Result<List<Flashcard>> {
        val source = sourceFor(sourceType, sourceId)
            ?: return Result.failure(Exception("Unsupported or invalid flashcard source."))
        val cacheKey = AiCacheKey(sourceType = sourceType, sourceId = sourceId, count = count)
        if (!forceRefresh) {
            flashcardsCache[cacheKey]?.let { return Result.success(it) }
        }

        val request = GenerateFlashcardsRequest(
            moduleId = source.moduleId,
            chapterId = source.chapterId,
            boardScanId = source.boardScanId,
            count = count
        )

        return apiResult("Flashcards", { aiApi.generateFlashcards(request) }) { flashcards ->
            flashcards.map { it.toDomain() }
                .also { cards -> flashcardsCache[cacheKey] = cards }
        }.withAiFailureMessage("Flashcard generation")
    }

    suspend fun generateQuiz(
        sourceType: String,
        sourceId: String,
        count: Int = 5,
        forceRefresh: Boolean = false
    ): Result<Quiz> {
        val source = sourceFor(sourceType, sourceId)
            ?: return Result.failure(Exception("Unsupported or invalid quiz source."))
        val cacheKey = AiCacheKey(sourceType = sourceType, sourceId = sourceId, count = count)
        if (!forceRefresh) {
            quizCache[cacheKey]?.let { return Result.success(it) }
        }

        val request = GenerateQuizRequest(
            moduleId = source.moduleId,
            chapterId = source.chapterId,
            boardScanId = source.boardScanId,
            count = count
        )

        return apiResult("Quiz", { aiApi.generateQuiz(request) }) { quiz ->
            quiz.toDomain()
                .also { generatedQuiz -> quizCache[cacheKey] = generatedQuiz }
        }.withAiFailureMessage("Quiz generation")
    }

    suspend fun startTutor(
        sourceType: String,
        sourceId: String,
        title: String? = null,
        forceRefresh: Boolean = false
    ): Result<TutorConversation> {
        val source = sourceFor(sourceType, sourceId)
            ?: return Result.failure(Exception("Unsupported or invalid tutor source."))
        val cacheKey = AiCacheKey(sourceType = sourceType, sourceId = sourceId)
        if (!forceRefresh) {
            tutorConversationCache[cacheKey]?.let { return Result.success(it) }
        }

        val request = StartTutorRequest(
            moduleId = source.moduleId,
            chapterId = source.chapterId,
            boardScanId = source.boardScanId,
            title = title
        )

        return apiResult("Tutor session", { aiApi.startTutor(request) }) {
            it.toDomain().toConversation()
                .also { conversation ->
                    tutorConversationCache[cacheKey] = conversation
                    tutorSessionCacheKeys[conversation.session.id] = cacheKey
                }
        }.withAiFailureMessage("Tutor session")
    }

    suspend fun sendTutorMessage(
        sessionId: String,
        message: String
    ): Result<TutorTurn> {
        val id = sessionId.toIntOrNull()
            ?: return Result.failure(Exception("Unsupported or invalid tutor session."))
        val request = TutorMessageRequest(
            sessionId = id,
            message = message
        )

        return apiResult("Tutor message", { aiApi.sendTutorMessage(request) }) {
            it.toDomain()
                .also { turn ->
                    val cacheKey = tutorSessionCacheKeys[turn.session.id]
                        ?: tutorSessionCacheKeys[sessionId]
                    if (cacheKey != null) {
                        val existingMessages = tutorConversationCache[cacheKey]?.messages.orEmpty()
                        tutorConversationCache[cacheKey] = TutorConversation(
                            session = turn.session,
                            messages = existingMessages + message.toCachedUserMessage(sessionId) + turn.message
                        )
                        tutorSessionCacheKeys[turn.session.id] = cacheKey
                    }
                }
        }.withAiFailureMessage("Tutor message")
    }

    override fun invalidateSource(sourceType: String, sourceId: String) {
        summaryCache.remove(AiCacheKey(sourceType = sourceType, sourceId = sourceId))
        flashcardsCache.keys.removeAll { it.matches(sourceType, sourceId) }
        quizCache.keys.removeAll { it.matches(sourceType, sourceId) }
        tutorConversationCache.keys.removeAll { it.matches(sourceType, sourceId) }
        tutorSessionCacheKeys.entries.removeAll { it.value.matches(sourceType, sourceId) }
    }
}

data class TutorTurn(
    val session: TutorSession,
    val message: TutorMessage
)

data class TutorConversation(
    val session: TutorSession,
    val messages: List<TutorMessage>
)

private data class AiCacheKey(
    val sourceType: String,
    val sourceId: String,
    val count: Int? = null
) {
    fun matches(sourceType: String, sourceId: String): Boolean {
        return this.sourceType == sourceType && this.sourceId == sourceId
    }
}

private fun TutorTurn.toConversation(): TutorConversation {
    return TutorConversation(
        session = session,
        messages = listOf(message)
    )
}

private fun String.toCachedUserMessage(sessionId: String): TutorMessage {
    return TutorMessage(
        id = "cached-user-${System.currentTimeMillis()}",
        sessionId = sessionId,
        role = "User",
        content = this
    )
}

private fun <T> Result<T>.withAiFailureMessage(action: String): Result<T> {
    if (isSuccess) return this

    val message = exceptionOrNull()?.message.orEmpty()
    val friendlyMessage = when {
        message.contains("timeout", ignoreCase = true) ->
            "$action took too long. Make sure Ollama is running, then try again."
        message.contains("failed to connect", ignoreCase = true) ||
            message.contains("unexpected end of stream", ignoreCase = true) ->
            "$action could not reach the local AI service. Check the backend and Ollama."
        else -> message.ifBlank { "$action failed. Please try again." }
    }

    return Result.failure(Exception(friendlyMessage))
}

private data class SourceIds(
    val moduleId: Int? = null,
    val chapterId: Int? = null,
    val boardScanId: Int? = null
)

private fun sourceFor(sourceType: String, sourceId: String): SourceIds? {
    val id = sourceId.toIntOrNull() ?: return null
    return when (sourceType) {
        "module" -> SourceIds(moduleId = id)
        "chapter" -> SourceIds(chapterId = id)
        "board_scan" -> SourceIds(boardScanId = id)
        else -> null
    }
}

private fun SummaryDto.toDomain(): Summary {
    val title = listOf(moduleTitle.orEmpty(), chapterTitle.orEmpty())
        .filter { it.isNotBlank() }
        .joinToString(" - ")
        .ifBlank { sourceType.toDisplayLabel() }

    return Summary(
        id = id.toString(),
        title = title,
        content = content,
        keyTakeaways = content.extractTakeaways(),
        sourceType = sourceType,
        moduleId = module?.toString(),
        moduleTitle = moduleTitle.orEmpty(),
        chapterId = chapter?.toString(),
        chapterTitle = chapterTitle.orEmpty(),
        boardScanId = boardScan?.toString(),
        isAiGenerated = isAiGenerated,
        createdAt = createdAt.toReadableDate()
    )
}

private fun FlashcardDto.toDomain(): Flashcard {
    return Flashcard(
        id = id.toString(),
        question = question,
        answer = answer,
        difficulty = difficulty.toDisplayLabel(),
        sourceType = sourceType,
        moduleTitle = moduleTitle.orEmpty(),
        chapterTitle = chapterTitle.orEmpty(),
        createdAt = createdAt.toReadableDate()
    )
}

private fun QuizDto.toDomain(): Quiz {
    return Quiz(
        id = id.toString(),
        title = title,
        description = description,
        sourceType = sourceType,
        questions = questions.sortedBy { it.order }.map { it.toDomain() },
        moduleTitle = moduleTitle.orEmpty(),
        chapterTitle = chapterTitle.orEmpty(),
        createdAt = createdAt.toReadableDate()
    )
}

private fun QuizQuestionDto.toDomain(): QuizQuestion {
    return QuizQuestion(
        id = id.toString(),
        question = question,
        questionType = questionType.toDisplayLabel(),
        choices = choices.orEmpty(),
        correctAnswer = correctAnswer,
        explanation = explanation,
        order = order
    )
}

private fun TutorResponseDto.toDomain(): TutorTurn {
    return TutorTurn(
        session = session.toDomain(),
        message = message.toDomain()
    )
}

private fun TutorSessionDto.toDomain(): TutorSession {
    return TutorSession(
        id = id.toString(),
        title = title,
        status = status.toDisplayLabel(),
        clearAnswersCount = clearAnswersCount,
        targetClearAnswers = targetClearAnswers,
        moduleTitle = moduleTitle.orEmpty(),
        chapterTitle = chapterTitle.orEmpty(),
        createdAt = createdAt.toReadableDate()
    )
}

private fun TutorMessageDto.toDomain(): TutorMessage {
    return TutorMessage(
        id = id.toString(),
        sessionId = session.toString(),
        role = role.toDisplayLabel(),
        content = content,
        clarityResult = clarityResult.orEmpty().toDisplayLabel(),
        createdAt = createdAt.toReadableDate()
    )
}

private fun String.extractTakeaways(): List<String> {
    return lines()
        .map { it.trim().trimStart('-', '*').trim() }
        .filter { it.length >= 12 }
        .take(4)
}
