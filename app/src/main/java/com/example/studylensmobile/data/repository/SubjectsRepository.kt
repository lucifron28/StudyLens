package com.example.studylensmobile.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.core.format.toReadableDate
import com.example.studylensmobile.core.utils.displayName
import com.example.studylensmobile.data.local.dao.ModuleDao
import com.example.studylensmobile.data.local.dao.SubjectDao
import com.example.studylensmobile.data.local.entity.toDomain
import com.example.studylensmobile.data.local.entity.toEntity
import com.example.studylensmobile.data.remote.apiResult
import com.example.studylensmobile.data.remote.emptyApiResult
import com.example.studylensmobile.data.remote.api.LearningApi
import com.example.studylensmobile.data.remote.dto.ModuleDto
import com.example.studylensmobile.data.remote.dto.ModuleWriteRequest
import com.example.studylensmobile.data.remote.dto.SubjectBoardScanPreviewDto
import com.example.studylensmobile.data.remote.dto.SubjectDto
import com.example.studylensmobile.data.remote.dto.SubjectModulePreviewDto
import com.example.studylensmobile.data.remote.dto.SubjectOverviewDto
import com.example.studylensmobile.data.remote.dto.StudyTaskPreviewDto
import com.example.studylensmobile.data.remote.dto.SubjectWriteRequest
import com.example.studylensmobile.domain.model.Subject
import com.example.studylensmobile.domain.model.SubjectBoardScanPreview
import com.example.studylensmobile.domain.model.SubjectModulePreview
import com.example.studylensmobile.domain.model.SubjectOverview
import com.example.studylensmobile.domain.model.StudyTaskPreview
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

class SubjectsRepository(
    private val learningApi: LearningApi,
    private val contentResolver: ContentResolver,
    private val aiCacheInvalidator: AiCacheInvalidator = AiCacheInvalidator { _, _ -> },
    private val subjectDao: SubjectDao,
    private val moduleDao: ModuleDao
) {
    suspend fun getSubjects(search: String? = null): Result<List<Subject>> {
        val networkResult = apiResult(
            label = "Subjects",
            call = { learningApi.getSubjects(search = search?.takeIf { it.isNotBlank() }) }
        ) { body ->
            body.results.map { it.toDomain() }
        }
        if (networkResult.isSuccess) {
            val subjects = networkResult.getOrThrow()
            // Only replace cache when fetching without search filter
            if (search.isNullOrBlank()) {
                subjectDao.deleteAll()
                subjectDao.upsertAll(subjects.map { it.toEntity() })
            }
            return networkResult
        }
        // Offline fallback: return whatever is in the cache
        val cached = subjectDao.getAll()
        return if (cached.isNotEmpty()) {
            Result.success(cached.map { it.toDomain() })
        } else {
            networkResult // propagate the original error if cache is empty
        }
    }

    suspend fun getSubjectOverview(subjectId: String): Result<SubjectOverview> {
        return apiResult("Subject overview", { learningApi.getSubjectOverview(subjectId) }) {
            it.toDomain()
        }
    }

    suspend fun getSubjectModules(subjectId: String): Result<List<SubjectModulePreview>> {
        return apiResult(
            label = "Modules",
            call = { learningApi.getModules(subjectId = subjectId) }
        ) { body ->
            body.results.map { it.toSubjectModulePreview() }
        }
    }

    suspend fun createSubject(
        title: String,
        description: String,
        color: String = ""
    ): Result<Unit> {
        return apiResult(
            label = "Create subject",
            call = {
                learningApi.createSubject(
                    SubjectWriteRequest(
                        title = title.trim(),
                        description = description.trim(),
                        color = color.trim()
                    )
                )
            }
        ) {
            Unit
        }
    }

    suspend fun updateSubject(
        subjectId: String,
        title: String,
        description: String,
        color: String = ""
    ): Result<Unit> {
        return apiResult(
            label = "Update subject",
            call = {
                learningApi.updateSubject(
                    subjectId = subjectId,
                    request = SubjectWriteRequest(
                        title = title.trim(),
                        description = description.trim(),
                        color = color.trim()
                    )
                )
            }
        ) {
            Unit
        }
    }

    suspend fun deleteSubject(subjectId: String): Result<Unit> {
        return emptyApiResult(
            label = "Delete subject",
            call = { learningApi.deleteSubject(subjectId) }
        )
    }

    suspend fun createModule(
        subjectId: String,
        title: String,
        description: String,
        contentType: String,
        markdownContent: String,
        fileUri: Uri? = null
    ): Result<Unit> {
        return apiResult(
            label = "Create module",
            call = {
                if (fileUri != null) {
                    val filePart = contentResolver.toFilePart(fileUri, "module_file")
                    val fields = mutableMapOf<String, okhttp3.RequestBody>()
                    fields["subject"] = subjectId.toRequestBody("text/plain".toMediaTypeOrNull())
                    fields["title"] = title.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                    if (description.isNotBlank()) fields["description"] = description.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                    fields["content_type"] = contentType.toApiContentType().toRequestBody("text/plain".toMediaTypeOrNull())
                    if (markdownContent.isNotBlank()) fields["markdown_content"] = markdownContent.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                    
                    learningApi.createModuleWithFile(filePart, fields)
                } else {
                    learningApi.createModule(
                        ModuleWriteRequest(
                            subject = subjectId.toIntOrNull(),
                            title = title.trim(),
                            description = description.trim(),
                            contentType = contentType.toApiContentType(),
                            markdownContent = markdownContent.trim()
                        )
                    )
                }
            }
        ) {
            Unit
        }
    }

    suspend fun updateModule(
        moduleId: String,
        title: String,
        description: String,
        contentType: String,
        markdownContent: String? = null,
        fileUri: Uri? = null
    ): Result<Unit> {
        return apiResult(
            label = "Update module",
            call = {
                if (fileUri != null) {
                    val filePart = contentResolver.toFilePart(fileUri, "module_file")
                    val fields = mutableMapOf<String, okhttp3.RequestBody>()
                    fields["title"] = title.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                    if (description.isNotBlank()) fields["description"] = description.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                    fields["content_type"] = contentType.toApiContentType().toRequestBody("text/plain".toMediaTypeOrNull())
                    if (!markdownContent.isNullOrBlank()) fields["markdown_content"] = markdownContent.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                    
                    learningApi.updateModuleWithFile(moduleId, filePart, fields)
                } else {
                    learningApi.updateModule(
                        moduleId = moduleId,
                        request = ModuleWriteRequest(
                            title = title.trim(),
                            description = description.trim(),
                            contentType = contentType.toApiContentType(),
                            markdownContent = markdownContent?.trim()?.takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        ) {
            Unit
        }.onSuccess {
            aiCacheInvalidator.invalidateSource("module", moduleId)
        }
    }

    suspend fun deleteModule(moduleId: String): Result<Unit> {
        return emptyApiResult(
            label = "Delete module",
            call = { learningApi.deleteModule(moduleId) }
        ).onSuccess {
            aiCacheInvalidator.invalidateSource("module", moduleId)
        }
    }

    suspend fun createTask(
        subjectId: String,
        title: String,
        content: String,
        taskType: String,
        isCompleted: Boolean = false,
        dueDate: String? = null,
        isPinned: Boolean = false
    ): Result<Unit> {
        return apiResult(
            label = "Create task",
            call = {
                learningApi.createStudyTask(
                    request = com.example.studylensmobile.data.remote.dto.StudyTaskWriteRequest(
                        subject = subjectId.toInt(),
                        title = title.trim(),
                        content = content.trim(),
                        taskType = taskType,
                        isCompleted = isCompleted,
                        dueDate = dueDate,
                        isPinned = isPinned
                    )
                )
            }
        ) { Unit }
    }

    suspend fun updateTask(
        taskId: String,
        subjectId: String,
        title: String,
        content: String,
        taskType: String,
        isCompleted: Boolean,
        dueDate: String?,
        isPinned: Boolean
    ): Result<Unit> {
        return apiResult(
            label = "Update task",
            call = {
                learningApi.updateStudyTask(
                    taskId = taskId,
                    request = com.example.studylensmobile.data.remote.dto.StudyTaskWriteRequest(
                        subject = subjectId.toInt(),
                        title = title.trim(),
                        content = content.trim(),
                        taskType = taskType,
                        isCompleted = isCompleted,
                        dueDate = dueDate,
                        isPinned = isPinned
                    )
                )
            }
        ) { Unit }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        return emptyApiResult(
            label = "Delete task",
            call = { learningApi.deleteStudyTask(taskId) }
        )
    }

    private fun ContentResolver.toFilePart(uri: Uri, partName: String): MultipartBody.Part {
        val mediaType = getType(uri)?.toMediaTypeOrNull() ?: "application/octet-stream".toMediaType()
        val body = object : RequestBody() {
            override fun contentType() = mediaType

            override fun writeTo(sink: BufferedSink) {
                openInputStream(uri)?.source()?.use(sink::writeAll)
                    ?: throw IOException("Unable to read the selected file.")
            }
        }
        return MultipartBody.Part.createFormData(partName, displayName(uri), body)
    }
}

private fun SubjectDto.toDomain(): Subject {
    return Subject(
        id = id.toString(),
        code = title.toSubjectCode(id),
        title = title,
        description = description,
        itemSummary = itemSummary.replace("|", "-")
    )
}

private fun SubjectOverviewDto.toDomain(): SubjectOverview {
    val subject = Subject(
        id = id.toString(),
        code = title.toSubjectCode(id),
        title = title,
        description = description,
        itemSummary = "$moduleCount Modules - $boardScanCount Notes - $taskCount Tasks"
    )

    return SubjectOverview(
        subject = subject,
        latestModules = latestModules.map { it.toDomain() },
        recentBoardScans = recentBoardScans.map { it.toDomain() },
        tasks = tasks.map { it.toDomain() }
    )
}

private fun SubjectModulePreviewDto.toDomain(): SubjectModulePreview {
    return SubjectModulePreview(
        id = id.toString(),
        title = title,
        description = description,
        contentType = contentType.toDisplayLabel(),
        isFavorite = isFavorite,
        updatedAt = updatedAt.toReadableDate()
    )
}

private fun ModuleDto.toSubjectModulePreview(): SubjectModulePreview {
    return SubjectModulePreview(
        id = id.toString(),
        title = title,
        description = description,
        contentType = contentType.toDisplayLabel(),
        isFavorite = isFavorite,
        updatedAt = updatedAt.toReadableDate()
    )
}

private fun SubjectBoardScanPreviewDto.toDomain(): SubjectBoardScanPreview {
    return SubjectBoardScanPreview(
        id = id.toString(),
        cleanedText = cleanedText,
        reviewStatus = reviewStatus.toDisplayLabel(),
        createdAt = createdAt.toReadableDate()
    )
}

private fun StudyTaskPreviewDto.toDomain(): StudyTaskPreview {
    return StudyTaskPreview(
        id = id.toString(),
        title = title,
        content = content,
        taskType = taskType.toDisplayLabel(),
        isCompleted = isCompleted,
        dueDate = dueDate?.toReadableDate(),
        isPinned = isPinned,
        createdAt = createdAt.toReadableDate()
    )
}

private fun String.toSubjectCode(id: Int): String {
    val letters = split(" ", "-", "_")
        .filter { it.isNotBlank() }
        .take(3)
        .joinToString("") { word -> word.first().uppercaseChar().toString() }
    return if (letters.isBlank()) "S$id" else "$letters$id"
}

private fun String.toApiContentType(): String {
    return trim()
        .lowercase()
        .replace(" ", "_")
        .ifBlank { "markdown" }
}
