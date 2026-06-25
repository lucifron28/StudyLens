package com.example.studylensmobile.domain.model

data class Dashboard(
    val overallProgress: Int,
    val stats: DashboardStats,
    val upcoming: List<DashboardUpcomingItem>,
    val continueLearning: List<DashboardContinueLearningItem>,
    val recentActivity: List<DashboardActivityItem>
)

data class DashboardStats(
    val modulesInProgress: Int,
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

data class DashboardContinueLearningItem(
    val id: Int,
    val moduleId: Int,
    val moduleTitle: String,
    val lastPosition: String,
    val status: String,
    val lastReadAt: String
)

data class DashboardActivityItem(
    val type: String,
    val id: Int,
    val title: String,
    val description: String,
    val createdAt: String
)
