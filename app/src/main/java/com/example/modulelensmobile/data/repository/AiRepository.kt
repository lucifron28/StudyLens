package com.example.modulelensmobile.data.repository

import com.example.modulelensmobile.data.remote.api.AiApi
import com.example.modulelensmobile.data.remote.dto.SummaryDto
import com.example.modulelensmobile.data.remote.dto.SummaryRequest
import com.example.modulelensmobile.domain.model.Summary

class AiRepository(
    private val aiApi: AiApi
) {
    suspend fun generateSummary(sourceType: String, sourceId: String): Result<Summary> {
        val id = sourceId.toIntOrNull()
            ?: return Result.failure(Exception("Invalid summary source."))
        val request = when (sourceType) {
            "module" -> SummaryRequest(moduleId = id)
            "chapter" -> SummaryRequest(chapterId = id)
            "board_scan" -> SummaryRequest(boardScanId = id)
            else -> return Result.failure(Exception("Unsupported summary source: $sourceType."))
        }

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
