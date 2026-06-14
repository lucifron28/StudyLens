package com.example.studylensmobile.feature.studytools

import com.example.studylensmobile.domain.model.Flashcard

data class FlashcardsUiState(
    val isLoading: Boolean = true,
    val flashcards: List<Flashcard> = emptyList(),
    val errorMessage: String? = null
)
