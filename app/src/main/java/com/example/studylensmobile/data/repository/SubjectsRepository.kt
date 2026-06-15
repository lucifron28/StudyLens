package com.example.studylensmobile.data.repository

import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.core.format.toReadableDate
import com.example.studylensmobile.data.remote.apiResult
import com.example.studylensmobile.data.remote.emptyApiResult
import com.example.studylensmobile.data.remote.api.LearningApi
import com.example.studylensmobile.data.remote.dto.ModuleDto
import com.example.studylensmobile.data.remote.dto.ModuleWriteRequest
import com.example.studylensmobile.data.remote.dto.SubjectBoardScanPreviewDto
import com.example.studylensmobile.data.remote.dto.SubjectDto
import com.example.studylensmobile.data.remote.dto.SubjectModulePreviewDto
import com.example.studylensmobile.data.remote.dto.SubjectOverviewDto
import com.example.studylensmobile.data.remote.dto.SubjectPostPreviewDto
import com.example.studylensmobile.data.remote.dto.SubjectTaskPreviewDto
import com.example.studylensmobile.data.remote.dto.SubjectWriteRequest
import com.example.studylensmobile.domain.model.Subject
import com.example.studylensmobile.domain.model.SubjectBoardScanPreview
import com.example.studylensmobile.domain.model.SubjectModulePreview
import com.example.studylensmobile.domain.model.SubjectOverview
import com.example.studylensmobile.domain.model.SubjectPostPreview
import com.example.studylensmobile.domain.model.SubjectTaskPreview

class SubjectsRepository(
    private val learningApi: LearningApi
) {
    suspend fun getSubjects(search: String? = null): Result<List<Subject>> {
        return apiResult(
            label = "Subjects",
            call = { learningApi.getSubjects(search = search?.takeIf { it.isNotBlank() }) }
        ) { body ->
            body.results.map { it.toDomain() }
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
        markdownContent: String
    ): Result<Unit> {
        return apiResult(
            label = "Create module",
            call = {
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
        ) {
            Unit
        }
    }

    suspend fun updateModule(
        moduleId: String,
        title: String,
        description: String,
        contentType: String,
        markdownContent: String? = null
    ): Result<Unit> {
        return apiResult(
            label = "Update module",
            call = {
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
        ) {
            Unit
        }
    }

    suspend fun deleteModule(moduleId: String): Result<Unit> {
        return emptyApiResult(
            label = "Delete module",
            call = { learningApi.deleteModule(moduleId) }
        )
    }
}

private fun SubjectDto.toDomain(): Subject {
    return Subject(
        id = id.toString(),
        code = title.toSubjectCode(id),
        title = title,
        description = description,
        itemSummary = itemSummary.replace("|", "-"),
        progressPercentage = progressPercentage.coerceIn(0, 100)
    )
}

private fun SubjectOverviewDto.toDomain(): SubjectOverview {
    val subject = Subject(
        id = id.toString(),
        code = title.toSubjectCode(id),
        title = title,
        description = description,
        itemSummary = "$moduleCount Modules - $taskCount Tasks - $boardScanCount Notes",
        progressPercentage = progressPercentage.coerceIn(0, 100)
    )

    return SubjectOverview(
        subject = subject,
        latestModules = latestModules.map { it.toDomain() },
        upcomingTasks = upcomingTasks.map { it.toDomain() },
        recentBoardScans = recentBoardScans.map { it.toDomain() },
        latestPosts = latestPosts.map { it.toDomain() }
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

private fun SubjectTaskPreviewDto.toDomain(): SubjectTaskPreview {
    return SubjectTaskPreview(
        id = id.toString(),
        title = title,
        taskType = taskType.toDisplayLabel(),
        status = status.toDisplayLabel(),
        priority = priority.toDisplayLabel(),
        dueAt = dueAt?.toReadableDate()
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

private fun SubjectPostPreviewDto.toDomain(): SubjectPostPreview {
    return SubjectPostPreview(
        id = id.toString(),
        title = title,
        content = content,
        postType = postType.toDisplayLabel(),
        isPinned = isPinned,
        postedAt = postedAt.toReadableDate()
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
