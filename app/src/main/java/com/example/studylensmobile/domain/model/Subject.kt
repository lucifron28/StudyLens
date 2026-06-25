package com.example.studylensmobile.domain.model

data class Subject(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val itemSummary: String
)

data class SubjectOverview(
    val subject: Subject,
    val latestModules: List<SubjectModulePreview>,
    val recentBoardScans: List<SubjectBoardScanPreview>,
    val tasks: List<StudyTaskPreview>
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

data class StudyTaskPreview(
    val id: String,
    val title: String,
    val content: String,
    val taskType: String,
    val isCompleted: Boolean,
    val dueDate: String?,
    val isPinned: Boolean,
    val createdAt: String
)
