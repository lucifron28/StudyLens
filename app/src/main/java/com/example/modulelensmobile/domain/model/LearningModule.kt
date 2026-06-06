package com.example.modulelensmobile.domain.model

data class LearningModule(
    val id: String,
    val subjectId: String,
    val title: String,
    val contentPreview: String,
    val progressPercentage: Int,
    val subjectTitle: String = "",
    val description: String = "",
    val contentType: String = "",
    val markdownContent: String = "",
    val extractedText: String = "",
    val moduleFileUrl: String? = null,
    val isFavorite: Boolean = false,
    val updatedAt: String = "",
    val chapters: List<LearningChapter> = emptyList()
)

data class LearningChapter(
    val id: String,
    val moduleId: String,
    val title: String,
    val order: Int,
    val markdownContent: String,
    val extractedText: String,
    val updatedAt: String
)
