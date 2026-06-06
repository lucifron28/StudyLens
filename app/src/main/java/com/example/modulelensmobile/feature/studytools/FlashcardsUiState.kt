package com.example.modulelensmobile.feature.studytools

import com.example.modulelensmobile.domain.model.Flashcard

data class FlashcardsUiState(
    val isLoading: Boolean = true,
    val flashcards: List<Flashcard> = emptyList(),
    val errorMessage: String? = null
)
