package com.example.studylensmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DashboardDto(
    @SerializedName("overall_progress") val overallProgress: Int,
    val stats: DashboardStatsDto,
    val upcoming: List<DashboardUpcomingItemDto>,
    @SerializedName("recent_board_scans") val recentBoardScans: List<DashboardBoardScanDto>,
    @SerializedName("recent_activity") val recentActivity: List<DashboardActivityItemDto>
)

data class DashboardStatsDto(
    @SerializedName("pending_tasks") val pendingTasks: Int,
    @SerializedName("notes_saved") val notesSaved: Int,
    @SerializedName("quizzes_completed") val quizzesCompleted: Int
)

data class DashboardUpcomingItemDto(
    val type: String,
    val id: Int,
    val title: String,
    val description: String,
    val subject: Int?,
    @SerializedName("subject_title") val subjectTitle: String,
    @SerializedName("task_type") val taskType: String? = null,
    @SerializedName("is_completed") val isCompleted: Boolean = false,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("posted_at") val postedAt: String? = null
)

data class DashboardBoardScanDto(
    val id: Int,
    val subject: Int?,
    @SerializedName("subject_title") val subjectTitle: String,
    @SerializedName("module_title") val moduleTitle: String,
    val summary: String,
    @SerializedName("review_status") val reviewStatus: String,
    @SerializedName("created_at") val createdAt: String
)

data class DashboardActivityItemDto(
    val type: String,
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("created_at") val createdAt: String
)

