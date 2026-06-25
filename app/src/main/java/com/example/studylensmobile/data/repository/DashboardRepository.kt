package com.example.studylensmobile.data.repository

import com.example.studylensmobile.core.format.toReadableDate
import com.example.studylensmobile.data.local.dao.BoardScanDao
import com.example.studylensmobile.data.local.dao.SubjectDao
import com.example.studylensmobile.data.remote.apiResult
import com.example.studylensmobile.data.remote.api.LearningApi
import com.example.studylensmobile.data.remote.dto.DashboardActivityItemDto
import com.example.studylensmobile.data.remote.dto.DashboardBoardScanDto
import com.example.studylensmobile.data.remote.dto.DashboardDto
import com.example.studylensmobile.data.remote.dto.DashboardUpcomingItemDto
import com.example.studylensmobile.domain.model.Dashboard
import com.example.studylensmobile.domain.model.DashboardActivityItem
import com.example.studylensmobile.domain.model.DashboardBoardScanItem
import com.example.studylensmobile.domain.model.DashboardStats
import com.example.studylensmobile.domain.model.DashboardUpcomingItem

class DashboardRepository(
    private val learningApi: LearningApi,
    private val subjectDao: SubjectDao,
    private val boardScanDao: BoardScanDao
) {
    suspend fun getDashboard(): Result<Dashboard> {
        val networkResult = apiResult("Dashboard", learningApi::getDashboard) { it.toDomain() }
        if (networkResult.isSuccess) return networkResult

        // Offline fallback: construct a minimal dashboard from local cache
        val cachedSubjects = subjectDao.getAll()
        val cachedScans = boardScanDao.getAll()
        return if (cachedSubjects.isNotEmpty() || cachedScans.isNotEmpty()) {
            Result.success(
                Dashboard(
                    overallProgress = 0,
                    stats = DashboardStats(
                        pendingTasks = 0,
                        notesSaved = cachedScans.size,
                        quizzesCompleted = 0
                    ),
                    upcoming = emptyList(),
                    recentBoardScans = emptyList(),
                    recentActivity = emptyList()
                )
            )
        } else {
            networkResult
        }
    }

    suspend fun toggleTaskCompletion(taskId: Int, isCompleted: Boolean): Result<Unit> {
        return apiResult(
            label = "Toggle task completion",
            call = { 
                learningApi.patchStudyTaskCompletion(
                    taskId = taskId.toString(),
                    request = com.example.studylensmobile.data.remote.dto.StudyTaskCompletionRequest(isCompleted = isCompleted)
                ) 
            }
        ) { Unit }
    }
}

private fun DashboardDto.toDomain(): Dashboard {
    return Dashboard(
        overallProgress = overallProgress,
        stats = DashboardStats(
            pendingTasks = stats.pendingTasks,
            notesSaved = stats.notesSaved,
            quizzesCompleted = stats.quizzesCompleted
        ),
        upcoming = upcoming.map { it.toDomain() },
        recentBoardScans = recentBoardScans.map { it.toDomain() },
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
        taskType = taskType.orEmpty(),
        isCompleted = isCompleted,
        dueDate = dueDate?.toReadableDate(),
        postedAt = postedAt?.toReadableDate()
    )
}

private fun DashboardBoardScanDto.toDomain(): DashboardBoardScanItem {
    return DashboardBoardScanItem(
        id = id,
        subjectId = subject,
        subjectTitle = subjectTitle,
        moduleTitle = moduleTitle,
        summary = summary,
        reviewStatus = reviewStatus,
        createdAt = createdAt.toReadableDate()
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
