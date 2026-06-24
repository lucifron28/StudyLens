package com.example.studylensmobile.domain.model

data class LearningModule(
    val id: String,
    val subjectId: String,
    val title: String,
    val contentPreview: String,
    val subjectTitle: String = "",
    val description: String = "",
    val contentType: String = "",
    val markdownContent: String = "",
    val extractedText: String = "",
    val moduleFileUrl: String? = null,
    val isFavorite: Boolean = false,
    val updatedAt: String = ""
)
