package com.example.modulelensmobile.domain.model

data class LearningModule(
    val id: String,
    val subjectId: String,
    val title: String,
    val contentPreview: String,
    val progressPercentage: Int
)
