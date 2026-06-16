package com.example.studylensmobile.feature.studytools

import com.example.studylensmobile.domain.model.Flashcard

data class FlashcardsUiState(
    val isLoading: Boolean = true,
    val flashcards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val isAnswerVisible: Boolean = false,
    val requestedCount: Int = 5,
    val errorMessage: String? = null
) {
    val currentFlashcard: Flashcard?
        get() = flashcards.getOrNull(currentIndex)

    val canGoPrevious: Boolean
        get() = currentIndex > 0

    val canGoNext: Boolean
        get() = currentIndex < flashcards.lastIndex

    val progressLabel: String
        get() = if (flashcards.isEmpty()) "0 / 0" else "${currentIndex + 1} / ${flashcards.size}"
}
