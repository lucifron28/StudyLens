package com.example.studylensmobile.feature.studytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiSummaryViewModel(
    private val sourceType: String,
    private val sourceId: String,
    private val aiRepository: AiRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiSummaryUiState())
    val uiState: StateFlow<AiSummaryUiState> = _uiState.asStateFlow()

    init {
        generateSummary(forceRefresh = false)
    }

    fun generateSummary(forceRefresh: Boolean = true) {
        viewModelScope.launch {
            _uiState.update {
                val hasSummary = it.summary != null
                it.copy(
                    isLoading = !hasSummary,
                    isRefreshing = hasSummary,
                    errorMessage = null
                )
            }

            val result = aiRepository.generateSummary(
                sourceType = sourceType,
                sourceId = sourceId,
                forceRefresh = forceRefresh
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    summary = result.getOrNull() ?: it.summary,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
}
