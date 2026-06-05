package com.example.modulelensmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DashboardDto(
    @SerializedName("overall_progress") val overallProgress: Int,
    val stats: DashboardStatsDto,
    val upcoming: List<DashboardUpcomingItemDto>,
    @SerializedName("continue_learning") val continueLearning: List<DashboardContinueLearningDto>,
    @SerializedName("recent_activity") val recentActivity: List<DashboardActivityItemDto>
)

data class DashboardStatsDto(
    @SerializedName("modules_in_progress") val modulesInProgress: Int,
    @SerializedName("notes_saved") val notesSaved: Int,
    @SerializedName("quizzes_completed") val quizzesCompleted: Int,
    @SerializedName("pending_tasks") val pendingTasks: Int
)

data class DashboardUpcomingItemDto(
    val type: String,
    val id: Int,
    val title: String,
    val description: String,
    val subject: Int?,
    @SerializedName("subject_title") val subjectTitle: String,
    val module: Int? = null,
    @SerializedName("module_title") val moduleTitle: String? = null,
    val status: String? = null,
    val priority: String? = null,
    @SerializedName("due_at") val dueAt: String? = null,
    @SerializedName("posted_at") val postedAt: String? = null
)

data class DashboardContinueLearningDto(
    val id: Int,
    val module: Int,
    @SerializedName("module_title") val moduleTitle: String,
    val chapter: Int?,
    @SerializedName("chapter_title") val chapterTitle: String,
    @SerializedName("progress_percentage") val progressPercentage: Int,
    @SerializedName("last_position") val lastPosition: String,
    val status: String,
    @SerializedName("last_read_at") val lastReadAt: String
)

data class DashboardActivityItemDto(
    val type: String,
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("created_at") val createdAt: String
)

