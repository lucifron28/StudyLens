package com.example.studylensmobile.feature.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.SubjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubjectsViewModel(
    private val subjectsRepository: SubjectsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubjectsUiState())
    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadSubjects() {
        viewModelScope.launch {
            val hasContent = _uiState.value.subjects.isNotEmpty()
            _uiState.update {
                it.copy(
                    isLoading = !hasContent,
                    isRefreshing = hasContent,
                    errorMessage = null
                )
            }

            val result = subjectsRepository.getSubjects(_uiState.value.searchQuery)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    subjects = result.getOrNull() ?: it.subjects,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
}
