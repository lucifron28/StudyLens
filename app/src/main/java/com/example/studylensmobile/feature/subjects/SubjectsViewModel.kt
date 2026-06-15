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
            refreshSubjects()
        }
    }

    fun createSubject(
        title: String,
        description: String,
        onSaved: () -> Unit = {}
    ) {
        mutateSubject(
            validateTitle = title,
            action = { subjectsRepository.createSubject(title, description) },
            onSaved = onSaved
        )
    }

    fun updateSubject(
        subjectId: String,
        title: String,
        description: String,
        onSaved: () -> Unit = {}
    ) {
        mutateSubject(
            validateTitle = title,
            action = { subjectsRepository.updateSubject(subjectId, title, description) },
            onSaved = onSaved
        )
    }

    fun deleteSubject(
        subjectId: String,
        onDeleted: () -> Unit = {}
    ) {
        mutateSubject(
            validateTitle = "delete",
            action = { subjectsRepository.deleteSubject(subjectId) },
            onSaved = onDeleted
        )
    }

    private fun mutateSubject(
        validateTitle: String,
        action: suspend () -> Result<Unit>,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            if (validateTitle.trim().isBlank()) {
                _uiState.update { it.copy(errorMessage = "Subject title is required.") }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isMutating = true,
                    errorMessage = null
                )
            }

            val result = action()
            if (result.isSuccess) {
                onSaved()
                refreshSubjects(showRefreshing = true)
            } else {
                _uiState.update {
                    it.copy(
                        isMutating = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    private suspend fun refreshSubjects(showRefreshing: Boolean = true) {
        val hasContent = _uiState.value.subjects.isNotEmpty()
        _uiState.update {
            it.copy(
                isLoading = !hasContent,
                isRefreshing = showRefreshing && hasContent,
                errorMessage = null
            )
        }

        val result = subjectsRepository.getSubjects(_uiState.value.searchQuery)

        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                isMutating = false,
                subjects = result.getOrNull() ?: it.subjects,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }
}
