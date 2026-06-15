package com.example.studylensmobile.data.repository

import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.core.format.toPreview
import com.example.studylensmobile.core.format.toReadableDate
import com.example.studylensmobile.data.remote.apiResult
import com.example.studylensmobile.data.remote.emptyApiResult
import com.example.studylensmobile.data.remote.api.LearningApi
import com.example.studylensmobile.data.remote.dto.BoardScanDto
import com.example.studylensmobile.data.remote.dto.BoardScanTagDto
import com.example.studylensmobile.data.remote.dto.BoardScanUpdateRequest
import com.example.studylensmobile.data.remote.dto.BoardScanWriteRequest
import com.example.studylensmobile.domain.model.BoardScan
import com.example.studylensmobile.domain.model.BoardScanTag

class BoardScansRepository(
    private val learningApi: LearningApi
) {
    suspend fun getBoardScans(
        search: String? = null,
        reviewStatus: String? = null
    ): Result<List<BoardScan>> {
        return apiResult(
            label = "Board scans",
            call = {
                learningApi.getBoardScans(
                    search = search?.takeIf { it.isNotBlank() },
                    reviewStatus = reviewStatus?.takeIf { it.isNotBlank() }
                )
            }
        ) { body ->
            body.results.map { it.toDomain() }
        }
    }

    suspend fun getBoardScan(scanId: String): Result<BoardScan> {
        return apiResult("Board scan", { learningApi.getBoardScan(scanId) }) {
            it.toDomain()
        }
    }

    suspend fun updateBoardScan(
        scanId: String,
        cleanedText: String,
        reviewStatus: String = "reviewed"
    ): Result<BoardScan> {
        return apiResult(
            label = "Save",
            call = {
                learningApi.updateBoardScan(
                    scanId = scanId,
                    request = BoardScanUpdateRequest(
                        cleanedText = cleanedText,
                        reviewStatus = reviewStatus
                    )
                )
            }
        ) {
            it.toDomain()
        }
    }

    suspend fun createBoardScan(
        rawOcrText: String,
        cleanedText: String,
        summary: String,
        reviewStatus: String,
        subjectId: String? = null,
        moduleId: String? = null,
        chapterId: String? = null
    ): Result<Unit> {
        return apiResult(
            label = "Create note",
            call = {
                learningApi.createBoardScan(
                    BoardScanWriteRequest(
                        subject = subjectId.toNullableId(),
                        module = moduleId.toNullableId(),
                        chapter = chapterId.toNullableId(),
                        rawOcrText = rawOcrText.trim(),
                        cleanedText = cleanedText.trim(),
                        summary = summary.trim(),
                        reviewStatus = reviewStatus.toApiReviewStatus(),
                        tags = emptyList()
                    )
                )
            }
        ) {
            Unit
        }
    }

    suspend fun updateBoardScanDetails(
        scanId: String,
        rawOcrText: String,
        cleanedText: String,
        summary: String,
        reviewStatus: String,
        subjectId: String? = null,
        moduleId: String? = null,
        chapterId: String? = null
    ): Result<Unit> {
        return apiResult(
            label = "Update note",
            call = {
                learningApi.updateBoardScanDetails(
                    scanId = scanId,
                    request = BoardScanWriteRequest(
                        subject = subjectId.toNullableId(),
                        module = moduleId.toNullableId(),
                        chapter = chapterId.toNullableId(),
                        rawOcrText = rawOcrText.trim(),
                        cleanedText = cleanedText.trim(),
                        summary = summary.trim(),
                        reviewStatus = reviewStatus.toApiReviewStatus()
                    )
                )
            }
        ) {
            Unit
        }
    }

    suspend fun deleteBoardScan(scanId: String): Result<Unit> {
        return emptyApiResult(
            label = "Delete note",
            call = { learningApi.deleteBoardScan(scanId) }
        )
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

private fun String?.toNullableId(): Int? {
    return this?.trim()?.takeIf { it.isNotBlank() }?.toIntOrNull()
}

private fun String.toApiReviewStatus(): String {
    return trim()
        .lowercase()
        .replace(" ", "_")
        .ifBlank { "new" }
}
