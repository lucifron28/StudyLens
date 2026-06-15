package com.example.studylensmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaginatedSubjectsDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<SubjectDto>
)

data class SubjectDto(
    val id: Int,
    val title: String,
    val description: String,
    val color: String?,
    @SerializedName("module_count") val moduleCount: Int,
    @SerializedName("task_count") val taskCount: Int,
    @SerializedName("board_scan_count") val boardScanCount: Int,
    @SerializedName("post_count") val postCount: Int,
    @SerializedName("progress_percentage") val progressPercentage: Int,
    @SerializedName("item_summary") val itemSummary: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class SubjectWriteRequest(
    val title: String,
    val description: String = "",
    val color: String = ""
)

data class SubjectOverviewDto(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("module_count") val moduleCount: Int,
    @SerializedName("task_count") val taskCount: Int,
    @SerializedName("board_scan_count") val boardScanCount: Int,
    @SerializedName("post_count") val postCount: Int,
    @SerializedName("progress_percentage") val progressPercentage: Int,
    @SerializedName("latest_modules") val latestModules: List<SubjectModulePreviewDto>,
    @SerializedName("upcoming_tasks") val upcomingTasks: List<SubjectTaskPreviewDto>,
    @SerializedName("recent_board_scans") val recentBoardScans: List<SubjectBoardScanPreviewDto>,
    @SerializedName("latest_posts") val latestPosts: List<SubjectPostPreviewDto>
)

data class SubjectModulePreviewDto(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("content_type") val contentType: String,
    @SerializedName("is_favorite") val isFavorite: Boolean,
    @SerializedName("updated_at") val updatedAt: String
)

data class SubjectTaskPreviewDto(
    val id: Int,
    val title: String,
    @SerializedName("task_type") val taskType: String,
    val status: String,
    val priority: String,
    @SerializedName("due_at") val dueAt: String?
)

data class SubjectBoardScanPreviewDto(
    val id: Int,
    @SerializedName("cleaned_text") val cleanedText: String,
    @SerializedName("review_status") val reviewStatus: String,
    @SerializedName("created_at") val createdAt: String
)

data class SubjectPostPreviewDto(
    val id: Int,
    val title: String,
    val content: String,
    @SerializedName("post_type") val postType: String,
    @SerializedName("is_pinned") val isPinned: Boolean,
    @SerializedName("posted_at") val postedAt: String
)
