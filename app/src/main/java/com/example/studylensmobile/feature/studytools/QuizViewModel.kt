package com.example.studylensmobile.feature.studytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuizViewModel(
    private val sourceType: String,
    private val sourceId: String,
    private val aiRepository: AiRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        generateQuiz()
    }

    fun generateQuiz() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result = aiRepository.generateQuiz(
                sourceType = sourceType,
                sourceId = sourceId
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    quiz = result.getOrNull() ?: it.quiz,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
}
