package com.example.studylensmobile.feature.studytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.AiRepository
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
        generateFlashcards(forceRefresh = false)
    }

    fun generateFlashcards(forceRefresh: Boolean = true) {
        viewModelScope.launch {
            val count = _uiState.value.requestedCount

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result = aiRepository.generateFlashcards(
                sourceType = sourceType,
                sourceId = sourceId,
                count = count,
                forceRefresh = forceRefresh
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    flashcards = result.getOrNull() ?: it.flashcards,
                    currentIndex = 0,
                    isAnswerVisible = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun setRequestedCount(count: Int) {
        _uiState.update {
            it.copy(requestedCount = count.coerceIn(MIN_FLASHCARDS, MAX_FLASHCARDS))
        }
    }

    fun toggleAnswer() {
        _uiState.update { it.copy(isAnswerVisible = !it.isAnswerVisible) }
    }

    fun previousCard() {
        _uiState.update {
            it.copy(
                currentIndex = (it.currentIndex - 1).coerceAtLeast(0),
                isAnswerVisible = false
            )
        }
    }

    fun nextCard() {
        _uiState.update {
            it.copy(
                currentIndex = (it.currentIndex + 1).coerceAtMost(it.flashcards.lastIndex.coerceAtLeast(0)),
                isAnswerVisible = false
            )
        }
    }

    private companion object {
        const val MIN_FLASHCARDS = 3
        const val MAX_FLASHCARDS = 10
    }
}
