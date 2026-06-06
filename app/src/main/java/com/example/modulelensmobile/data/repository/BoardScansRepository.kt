package com.example.modulelensmobile.data.repository

import com.example.modulelensmobile.data.remote.api.LearningApi
import com.example.modulelensmobile.data.remote.dto.BoardScanDto
import com.example.modulelensmobile.data.remote.dto.BoardScanTagDto
import com.example.modulelensmobile.data.remote.dto.BoardScanUpdateRequest
import com.example.modulelensmobile.domain.model.BoardScan
import com.example.modulelensmobile.domain.model.BoardScanTag

class BoardScansRepository(
    private val learningApi: LearningApi
) {
    suspend fun getBoardScans(
        search: String? = null,
        reviewStatus: String? = null
    ): Result<List<BoardScan>> {
        return try {
            val response = learningApi.getBoardScans(
                search = search?.takeIf { it.isNotBlank() },
                reviewStatus = reviewStatus?.takeIf { it.isNotBlank() }
            )
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Board scans failed: empty server response."))
                Result.success(body.results.map { it.toDomain() })
            } else {
                Result.failure(Exception("Board scans failed (${response.code()}). Please try again."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun getBoardScan(scanId: String): Result<BoardScan> {
        return try {
            val response = learningApi.getBoardScan(scanId)
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Board scan failed: empty server response."))
                Result.success(body.toDomain())
            } else {
                Result.failure(Exception("Board scan failed (${response.code()}). Please try again."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun updateBoardScan(
        scanId: String,
        cleanedText: String,
        reviewStatus: String = "reviewed"
    ): Result<BoardScan> {
        return try {
            val response = learningApi.updateBoardScan(
                scanId = scanId,
                request = BoardScanUpdateRequest(
                    cleanedText = cleanedText,
                    reviewStatus = reviewStatus
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Save failed: empty server response."))
                Result.success(body.toDomain())
            } else {
                Result.failure(Exception("Save failed (${response.code()}). Please try again."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}

private fun BoardScanDto.toDomain(): BoardScan {
    val displayTitle = chapterTitle
        ?.takeIf { it.isNotBlank() }
        ?: moduleTitle?.takeIf { it.isNotBlank() }
        ?: subjectTitle?.takeIf { it.isNotBlank() }
        ?: "Board Note #$id"
    val text = cleanedText.ifBlank { rawOcrText }

    return BoardScan(
        id = id.toString(),
        title = displayTitle,
        subjectCode = subjectTitle.orEmpty().toSubjectCode(subject),
        dateLabel = createdAt.toReadableDate(),
        reviewStatus = reviewStatus.toDisplayLabel(),
        previewText = text.toPreview(),
        subjectId = subject?.toString(),
        subjectTitle = subjectTitle.orEmpty(),
        moduleId = module?.toString(),
        moduleTitle = moduleTitle.orEmpty(),
        chapterId = chapter?.toString(),
        chapterTitle = chapterTitle.orEmpty(),
        imageUrl = imageUrl,
        rawOcrText = rawOcrText,
        cleanedText = cleanedText,
        summary = summary,
        tags = tagDetails.map { it.toDomain() },
        createdAt = createdAt.toReadableDate(),
        updatedAt = updatedAt.toReadableDate()
    )
}

private fun BoardScanTagDto.toDomain(): BoardScanTag {
    return BoardScanTag(
        id = id.toString(),
        name = name,
        color = color.orEmpty()
    )
}

private fun String.toSubjectCode(id: Int?): String {
    val letters = split(" ", "-", "_")
        .filter { it.isNotBlank() }
        .take(3)
        .joinToString("") { word -> word.first().uppercaseChar().toString() }
    return if (letters.isBlank()) "S${id ?: ""}" else letters
}

private fun String.toDisplayLabel(): String {
    return split("_", "-", " ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}

private fun String.toPreview(): String {
    return replace("\n", " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(140)
}

private fun String.toReadableDate(): String {
    return takeIf { it.length >= 10 }?.substring(0, 10) ?: this
}
