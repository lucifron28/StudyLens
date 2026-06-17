package com.example.studylensmobile.domain.model

data class Subject(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val itemSummary: String,
    val progressPercentage: Int
)

data class SubjectOverview(
    val subject: Subject,
    val latestModules: List<SubjectModulePreview>,
    val recentBoardScans: List<SubjectBoardScanPreview>,
    val latestPosts: List<SubjectPostPreview>
)

data class SubjectModulePreview(
    val id: String,
    val title: String,
    val description: String,
    val contentType: String,
    val isFavorite: Boolean,
    val updatedAt: String
)

data class SubjectBoardScanPreview(
    val id: String,
    val cleanedText: String,
    val reviewStatus: String,
    val createdAt: String
)

data class SubjectPostPreview(
    val id: String,
    val title: String,
    val content: String,
    val postType: String,
    val isPinned: Boolean,
    val postedAt: String
)
