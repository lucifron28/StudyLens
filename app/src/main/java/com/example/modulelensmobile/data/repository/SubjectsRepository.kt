package com.example.modulelensmobile.data.repository

import com.example.modulelensmobile.core.format.toDisplayLabel
import com.example.modulelensmobile.core.format.toReadableDate
import com.example.modulelensmobile.data.remote.apiResult
import com.example.modulelensmobile.data.remote.api.LearningApi
import com.example.modulelensmobile.data.remote.dto.SubjectBoardScanPreviewDto
import com.example.modulelensmobile.data.remote.dto.SubjectDto
import com.example.modulelensmobile.data.remote.dto.SubjectModulePreviewDto
import com.example.modulelensmobile.data.remote.dto.SubjectOverviewDto
import com.example.modulelensmobile.data.remote.dto.SubjectPostPreviewDto
import com.example.modulelensmobile.data.remote.dto.SubjectTaskPreviewDto
import com.example.modulelensmobile.domain.model.Subject
import com.example.modulelensmobile.domain.model.SubjectBoardScanPreview
import com.example.modulelensmobile.domain.model.SubjectModulePreview
import com.example.modulelensmobile.domain.model.SubjectOverview
import com.example.modulelensmobile.domain.model.SubjectPostPreview
import com.example.modulelensmobile.domain.model.SubjectTaskPreview

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
