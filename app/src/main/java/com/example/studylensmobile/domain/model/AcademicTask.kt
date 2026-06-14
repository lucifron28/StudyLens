package com.example.studylensmobile.domain.model

data class AcademicTask(
    val id: String,
    val title: String,
    val subjectCode: String,
    val dueDate: String,
    val isCompleted: Boolean
)
