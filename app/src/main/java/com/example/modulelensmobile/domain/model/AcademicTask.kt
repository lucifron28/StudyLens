package com.example.modulelensmobile.domain.model

data class AcademicTask(
    val id: String,
    val title: String,
    val subjectCode: String,
    val dueDate: String,
    val isCompleted: Boolean
)
