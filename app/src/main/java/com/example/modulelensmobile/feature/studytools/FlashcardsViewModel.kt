package com.example.modulelensmobile.feature.studytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modulelensmobile.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FlashcardsViewModel(
    private val sourceType: String,
    private val sourceId: String,
    private val aiRepository: AiRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FlashcardsUiState())
    val uiState: StateFlow<FlashcardsUiState> = _uiState.asStateFlow()

    init {
        generateFlashcards()
    }

    fun generateFlashcards() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result = aiRepository.generateFlashcards(
                sourceType = sourceType,
                sourceId = sourceId
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    flashcards = result.getOrNull() ?: it.flashcards,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
}
