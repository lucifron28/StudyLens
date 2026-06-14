package com.example.studylensmobile.domain.model

data class Flashcard(
    val id: String,
    val question: String,
    val answer: String,
    val difficulty: String,
    val sourceType: String,
    val moduleTitle: String = "",
    val chapterTitle: String = "",
    val createdAt: String = ""
)
