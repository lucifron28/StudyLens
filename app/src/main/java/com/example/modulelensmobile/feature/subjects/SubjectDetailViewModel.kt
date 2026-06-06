package com.example.modulelensmobile.feature.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modulelensmobile.data.repository.SubjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubjectDetailViewModel(
    private val subjectId: String,
    private val subjectsRepository: SubjectsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubjectDetailUiState())
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    init {
        loadOverview()
    }

    fun loadOverview() {
        viewModelScope.launch {
            val hasContent = _uiState.value.overview != null
            _uiState.update {
                it.copy(
                    isLoading = !hasContent,
                    isRefreshing = hasContent,
                    errorMessage = null
                )
            }

            val result = subjectsRepository.getSubjectOverview(subjectId)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    overview = result.getOrNull() ?: it.overview,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
}
