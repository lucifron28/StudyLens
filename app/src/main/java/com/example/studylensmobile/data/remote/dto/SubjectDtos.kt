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
    @SerializedName("board_scan_count") val boardScanCount: Int,
    @SerializedName("task_count") val taskCount: Int,
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
    @SerializedName("board_scan_count") val boardScanCount: Int,
    @SerializedName("task_count") val taskCount: Int,
    @SerializedName("latest_modules") val latestModules: List<SubjectModulePreviewDto>,
    @SerializedName("recent_board_scans") val recentBoardScans: List<SubjectBoardScanPreviewDto>,
    @SerializedName("tasks") val tasks: List<StudyTaskPreviewDto>
)

data class SubjectModulePreviewDto(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("content_type") val contentType: String,
    @SerializedName("is_favorite") val isFavorite: Boolean,
    @SerializedName("updated_at") val updatedAt: String
)

data class SubjectBoardScanPreviewDto(
    val id: Int,
    @SerializedName("cleaned_text") val cleanedText: String,
    @SerializedName("review_status") val reviewStatus: String,
    @SerializedName("created_at") val createdAt: String
)

data class StudyTaskPreviewDto(
    val id: Int,
    val title: String,
    val content: String,
    @SerializedName("task_type") val taskType: String,
    @SerializedName("is_completed") val isCompleted: Boolean,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("is_pinned") val isPinned: Boolean,
    @SerializedName("created_at") val createdAt: String
}

data class StudyTaskWriteRequest(
    val subject: Int,
    val title: String,
    val content: String,
    @SerializedName("task_type") val taskType: String,
    @SerializedName("is_completed") val isCompleted: Boolean = false,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("is_pinned") val isPinned: Boolean = false
)
