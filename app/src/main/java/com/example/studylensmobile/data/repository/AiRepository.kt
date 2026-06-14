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
import com.example.studylensmobile.data.remote.dto.SummaryDto
import com.example.studylensmobile.data.remote.dto.SummaryRequest
import com.example.studylensmobile.domain.model.Flashcard
import com.example.studylensmobile.domain.model.Quiz
import com.example.studylensmobile.domain.model.QuizQuestion
import com.example.studylensmobile.domain.model.Summary

class AiRepository(
    private val aiApi: AiApi
) {
    suspend fun generateSummary(sourceType: String, sourceId: String): Result<Summary> {
        val source = sourceFor(sourceType, sourceId)
            ?: return Result.failure(Exception("Unsupported or invalid summary source."))
        val request = SummaryRequest(
            moduleId = source.moduleId,
            chapterId = source.chapterId,
            boardScanId = source.boardScanId
        )

        return apiResult("Summary", { aiApi.summarize(request) }) {
            it.toDomain()
        }
    }

    suspend fun generateFlashcards(
        sourceType: String,
        sourceId: String,
        count: Int = 5
    ): Result<List<Flashcard>> {
        val source = sourceFor(sourceType, sourceId)
            ?: return Result.failure(Exception("Unsupported or invalid flashcard source."))
        val request = GenerateFlashcardsRequest(
            moduleId = source.moduleId,
            chapterId = source.chapterId,
            boardScanId = source.boardScanId,
            count = count
        )

        return apiResult("Flashcards", { aiApi.generateFlashcards(request) }) { flashcards ->
            flashcards.map { it.toDomain() }
        }
    }

    suspend fun generateQuiz(
        sourceType: String,
        sourceId: String,
        count: Int = 5
    ): Result<Quiz> {
        val source = sourceFor(sourceType, sourceId)
            ?: return Result.failure(Exception("Unsupported or invalid quiz source."))
        val request = GenerateQuizRequest(
            moduleId = source.moduleId,
            chapterId = source.chapterId,
            boardScanId = source.boardScanId,
            count = count
        )

        return apiResult("Quiz", { aiApi.generateQuiz(request) }) { quiz ->
            quiz.toDomain()
        }
    }
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

private fun String.extractTakeaways(): List<String> {
    return lines()
        .map { it.trim().trimStart('-', '*').trim() }
        .filter { it.length >= 12 }
        .take(4)
}
