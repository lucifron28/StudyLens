package com.example.studylensmobile.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
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
import com.example.studylensmobile.data.local.dao.BoardScanDao
import com.example.studylensmobile.data.local.entity.toDomain
import com.example.studylensmobile.data.local.entity.toEntity
import com.example.studylensmobile.domain.model.BoardScan
import com.example.studylensmobile.domain.model.BoardScanTag
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class BoardScansRepository(
    private val learningApi: LearningApi,
    private val contentResolver: ContentResolver,
    private val aiCacheInvalidator: AiCacheInvalidator = AiCacheInvalidator { _, _ -> },
    private val boardScanDao: BoardScanDao
) {
    suspend fun getBoardScans(
        search: String? = null,
        reviewStatus: String? = null
    ): Result<List<BoardScan>> {
        val networkResult = apiResult(
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
        if (networkResult.isSuccess) {
            val scans = networkResult.getOrThrow()
            if (search.isNullOrBlank() && reviewStatus.isNullOrBlank()) {
                boardScanDao.deleteAll()
                boardScanDao.upsertAll(scans.map { it.toEntity() })
            }
            return networkResult
        }
        val cached = boardScanDao.getAll()
        return if (cached.isNotEmpty()) {
            Result.success(cached.map { it.toDomain() })
        } else {
            networkResult
        }
    }

    suspend fun getBoardScan(scanId: String): Result<BoardScan> {
        val networkResult = apiResult("Board scan", { learningApi.getBoardScan(scanId) }) {
            it.toDomain()
        }
        if (networkResult.isSuccess) {
            boardScanDao.upsert(networkResult.getOrThrow().toEntity())
            return networkResult
        }
        val cached = boardScanDao.getById(scanId)
        return if (cached != null) Result.success(cached.toDomain()) else networkResult
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
        }.onSuccess {
            aiCacheInvalidator.invalidateSource("board_scan", scanId)
        }
    }

    suspend fun createBoardScan(
        rawOcrText: String,
        cleanedText: String,
        summary: String,
        reviewStatus: String,
        subjectId: String? = null,
        moduleId: String? = null,
        imageUri: String? = null
    ): Result<Unit> {
        val request = BoardScanWriteRequest(
            subject = subjectId.toNullableId(),
            module = moduleId.toNullableId(),
            rawOcrText = rawOcrText.trim(),
            cleanedText = cleanedText.trim(),
            summary = summary.trim(),
            reviewStatus = reviewStatus.toApiReviewStatus(),
            tags = emptyList()
        )

        return apiResult(
            label = "Create note",
            call = {
                imageUri?.takeIf { it.isNotBlank() }
                    ?.let {
                        learningApi.createBoardScanWithImage(
                            contentResolver.toImagePart(Uri.parse(it)),
                            request.toMultipartFields()
                        )
                    }
                    ?: learningApi.createBoardScan(request)
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
        imageUri: String? = null
    ): Result<Unit> {
        val request = BoardScanWriteRequest(
            subject = subjectId.toNullableId(),
            module = moduleId.toNullableId(),
            rawOcrText = rawOcrText.trim(),
            cleanedText = cleanedText.trim(),
            summary = summary.trim(),
            reviewStatus = reviewStatus.toApiReviewStatus()
        )

        return apiResult(
            label = "Update note",
            call = {
                imageUri?.takeIf { it.isNotBlank() }
                    ?.let {
                        learningApi.updateBoardScanWithImage(
                            scanId = scanId,
                            image = contentResolver.toImagePart(Uri.parse(it)),
                            fields = request.toMultipartFields(includeEmptyRelations = true)
                        )
                    }
                    ?: learningApi.updateBoardScanDetails(scanId, request.toJsonObject())
            }
        ) {
            Unit
        }.onSuccess {
            aiCacheInvalidator.invalidateSource("board_scan", scanId)
        }
    }

    suspend fun deleteBoardScan(scanId: String): Result<Unit> {
        return emptyApiResult(
            label = "Delete note",
            call = { learningApi.deleteBoardScan(scanId) }
        ).onSuccess {
            aiCacheInvalidator.invalidateSource("board_scan", scanId)
            boardScanDao.deleteById(scanId)
        }
    }
}

private fun BoardScanWriteRequest.toMultipartFields(
    includeEmptyRelations: Boolean = false
): Map<String, RequestBody> {
    return buildMap {
        subject?.let { put("subject", it.toString().toTextPart()) }
        module?.let { put("module", it.toString().toTextPart()) }
        if (includeEmptyRelations) {
            if (subject == null) put("subject", "".toTextPart())
            if (module == null) put("module", "".toTextPart())
        }
        rawOcrText?.let { put("raw_ocr_text", it.toTextPart()) }
        cleanedText?.let { put("cleaned_text", it.toTextPart()) }
        summary?.let { put("summary", it.toTextPart()) }
        reviewStatus?.let { put("review_status", it.toTextPart()) }
    }
}

private fun BoardScanWriteRequest.toJsonObject(): JsonObject {
    return JsonObject().apply {
        add("subject", subject.toJsonElement())
        add("module", module.toJsonElement())
        addProperty("raw_ocr_text", rawOcrText)
        addProperty("cleaned_text", cleanedText)
        addProperty("summary", summary)
        addProperty("review_status", reviewStatus)
    }
}

private fun Int?.toJsonElement(): JsonElement {
    return this?.let(::JsonPrimitive) ?: JsonNull.INSTANCE
}

private fun String.toTextPart(): RequestBody {
    return toRequestBody("text/plain".toMediaType())
}

private suspend fun ContentResolver.toImagePart(uri: Uri): MultipartBody.Part = withContext(Dispatchers.IO) {
    val bytes = openInputStream(uri)?.use { it.readBytes() }
        ?: throw IOException("Unable to read the selected image.")
    val mediaType = getType(uri)?.toMediaTypeOrNull()
        ?: "image/jpeg".toMediaType()
    val body = bytes.toRequestBody(mediaType)
    MultipartBody.Part.createFormData("image", "board-scan.jpg", body)
}

private fun BoardScanDto.toDomain(): BoardScan {
    val displayTitle = moduleTitle?.takeIf { it.isNotBlank() }
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
