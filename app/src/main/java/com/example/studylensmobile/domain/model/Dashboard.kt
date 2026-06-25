package com.example.studylensmobile.domain.model

data class Dashboard(
    val overallProgress: Int,
    val stats: DashboardStats,
    val upcoming: List<DashboardUpcomingItem>,
    val recentBoardScans: List<DashboardBoardScanItem>,
    val recentActivity: List<DashboardActivityItem>
)

data class DashboardStats(
    val pendingTasks: Int,
    val notesSaved: Int,
    val quizzesCompleted: Int
)

data class DashboardUpcomingItem(
    val type: String,
    val id: Int,
    val title: String,
    val description: String,
    val subjectId: Int?,
    val subjectTitle: String,
    val taskType: String,
    val isCompleted: Boolean,
    val dueDate: String?,
    val postedAt: String?
)

data class DashboardBoardScanItem(
    val id: Int,
    val subjectId: Int?,
    val subjectTitle: String,
    val moduleTitle: String,
    val summary: String,
    val reviewStatus: String,
    val createdAt: String
)

data class DashboardActivityItem(
    val type: String,
    val id: Int,
    val title: String,
    val description: String,
    val createdAt: String
)
