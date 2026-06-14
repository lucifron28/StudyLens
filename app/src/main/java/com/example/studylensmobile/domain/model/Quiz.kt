package com.example.studylensmobile.domain.model

data class Quiz(
    val id: String,
    val title: String,
    val description: String,
    val sourceType: String,
    val questions: List<QuizQuestion>,
    val moduleTitle: String = "",
    val chapterTitle: String = "",
    val createdAt: String = ""
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val questionType: String,
    val choices: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val order: Int
)
