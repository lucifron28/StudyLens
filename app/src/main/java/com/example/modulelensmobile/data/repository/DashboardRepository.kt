package com.example.modulelensmobile.data.repository

import com.example.modulelensmobile.data.remote.api.LearningApi
import com.example.modulelensmobile.data.remote.dto.DashboardActivityItemDto
import com.example.modulelensmobile.data.remote.dto.DashboardContinueLearningDto
import com.example.modulelensmobile.data.remote.dto.DashboardDto
import com.example.modulelensmobile.data.remote.dto.DashboardUpcomingItemDto
import com.example.modulelensmobile.domain.model.Dashboard
import com.example.modulelensmobile.domain.model.DashboardActivityItem
import com.example.modulelensmobile.domain.model.DashboardContinueLearningItem
import com.example.modulelensmobile.domain.model.DashboardStats
import com.example.modulelensmobile.domain.model.DashboardUpcomingItem

class DashboardRepository(
    private val learningApi: LearningApi
) {
    suspend fun getDashboard(): Result<Dashboard> {
        return try {
            val response = learningApi.getDashboard()
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Dashboard failed: empty server response."))
                Result.success(body.toDomain())
            } else {
                Result.failure(Exception("Dashboard failed (${response.code()}). Please try again."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}

private fun DashboardDto.toDomain(): Dashboard {
    return Dashboard(
        overallProgress = overallProgress,
        stats = DashboardStats(
            modulesInProgress = stats.modulesInProgress,
            notesSaved = stats.notesSaved,
            quizzesCompleted = stats.quizzesCompleted,
            pendingTasks = stats.pendingTasks
        ),
        upcoming = upcoming.map { it.toDomain() },
        continueLearning = continueLearning.map { it.toDomain() },
        recentActivity = recentActivity.map { it.toDomain() }
    )
}

private fun DashboardUpcomingItemDto.toDomain(): DashboardUpcomingItem {
    return DashboardUpcomingItem(
        type = type,
        id = id,
        title = title,
        description = description,
        subjectId = subject,
        subjectTitle = subjectTitle,
        moduleId = module,
        moduleTitle = moduleTitle.orEmpty(),
        status = status.orEmpty(),
        priority = priority.orEmpty(),
        dueAt = dueAt?.toReadableDate(),
        postedAt = postedAt?.toReadableDate()
    )
}

private fun DashboardContinueLearningDto.toDomain(): DashboardContinueLearningItem {
    return DashboardContinueLearningItem(
        id = id,
        moduleId = module,
        moduleTitle = moduleTitle,
        chapterId = chapter,
        chapterTitle = chapterTitle,
        progressPercentage = progressPercentage,
        lastPosition = lastPosition,
        status = status,
        lastReadAt = lastReadAt.toReadableDate()
    )
}

private fun DashboardActivityItemDto.toDomain(): DashboardActivityItem {
    return DashboardActivityItem(
        type = type,
        id = id,
        title = title,
        description = description,
        createdAt = createdAt.toReadableDate()
    )
}

private fun String.toReadableDate(): String {
    return takeIf { it.length >= 10 }?.substring(0, 10) ?: this
}

