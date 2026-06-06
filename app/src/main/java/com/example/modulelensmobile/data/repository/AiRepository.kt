package com.example.modulelensmobile.data.repository

import com.example.modulelensmobile.data.remote.api.AiApi
import com.example.modulelensmobile.data.remote.dto.FlashcardDto
import com.example.modulelensmobile.data.remote.dto.GenerateFlashcardsRequest
import com.example.modulelensmobile.data.remote.dto.GenerateQuizRequest
import com.example.modulelensmobile.data.remote.dto.QuizDto
import com.example.modulelensmobile.data.remote.dto.QuizQuestionDto
import com.example.modulelensmobile.data.remote.dto.SummaryDto
import com.example.modulelensmobile.data.remote.dto.SummaryRequest
import com.example.modulelensmobile.domain.model.Flashcard
import com.example.modulelensmobile.domain.model.Quiz
import com.example.modulelensmobile.domain.model.QuizQuestion
import com.example.modulelensmobile.domain.model.Summary

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

        return try {
            val response = aiApi.summarize(request)
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Summary failed: empty server response."))
                Result.success(body.toDomain())
            } else {
                Result.failure(Exception("Summary failed (${response.code()}). Please try again."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
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

        return try {
            val response = aiApi.generateFlashcards(request)
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Flashcards failed: empty server response."))
                Result.success(body.map { it.toDomain() })
            } else {
                Result.failure(Exception("Flashcards failed (${response.code()}). Please try again."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
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

        return try {
            val response = aiApi.generateQuiz(request)
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Quiz failed: empty server response."))
                Result.success(body.toDomain())
            } else {
                Result.failure(Exception("Quiz failed (${response.code()}). Please try again."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
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

private fun String.toDisplayLabel(): String {
    return split("_", "-", " ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}

private fun String.toReadableDate(): String {
    return takeIf { it.length >= 10 }?.substring(0, 10) ?: this
}
