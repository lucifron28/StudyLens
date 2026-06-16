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
            val count = _uiState.value.requestedCount

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result = aiRepository.generateQuiz(
                sourceType = sourceType,
                sourceId = sourceId,
                count = count
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    quiz = result.getOrNull() ?: it.quiz,
                    currentIndex = 0,
                    selectedAnswers = emptyMap(),
                    submittedQuestionIds = emptySet(),
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun setRequestedCount(count: Int) {
        _uiState.update {
            it.copy(requestedCount = count.coerceIn(MIN_QUESTIONS, MAX_QUESTIONS))
        }
    }

    fun selectAnswer(answer: String) {
        _uiState.update { state ->
            val question = state.currentQuestion ?: return@update state
            if (question.id in state.submittedQuestionIds) {
                state
            } else {
                state.copy(selectedAnswers = state.selectedAnswers + (question.id to answer))
            }
        }
    }

    fun submitCurrentAnswer() {
        _uiState.update { state ->
            val question = state.currentQuestion ?: return@update state
            if (state.selectedAnswers[question.id].isNullOrBlank()) {
                state
            } else {
                state.copy(submittedQuestionIds = state.submittedQuestionIds + question.id)
            }
        }
    }

    fun previousQuestion() {
        _uiState.update {
            it.copy(currentIndex = (it.currentIndex - 1).coerceAtLeast(0))
        }
    }

    fun nextQuestion() {
        _uiState.update {
            val lastIndex = it.quiz?.questions?.lastIndex ?: 0
            it.copy(currentIndex = (it.currentIndex + 1).coerceAtMost(lastIndex))
        }
    }

    fun restartQuiz() {
        _uiState.update {
            it.copy(
                currentIndex = 0,
                selectedAnswers = emptyMap(),
                submittedQuestionIds = emptySet()
            )
        }
    }

    private companion object {
        const val MIN_QUESTIONS = 3
        const val MAX_QUESTIONS = 10
    }
}
