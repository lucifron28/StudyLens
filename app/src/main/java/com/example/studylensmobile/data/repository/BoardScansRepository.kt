package com.example.studylensmobile.data.repository

import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.core.format.toPreview
import com.example.studylensmobile.core.format.toReadableDate
import com.example.studylensmobile.data.remote.apiResult
import com.example.studylensmobile.data.remote.api.LearningApi
import com.example.studylensmobile.data.remote.dto.BoardScanDto
import com.example.studylensmobile.data.remote.dto.BoardScanTagDto
import com.example.studylensmobile.data.remote.dto.BoardScanUpdateRequest
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
