package com.example.studylensmobile.feature.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.SubjectsRepository
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
            refreshOverview()
        }
    }

    fun createModule(
        title: String,
        description: String,
        contentType: String,
        markdownContent: String,
        onSaved: () -> Unit = {}
    ) {
        mutateModule(
            validateTitle = title,
            action = {
                subjectsRepository.createModule(
                    subjectId = subjectId,
                    title = title,
                    description = description,
                    contentType = contentType,
                    markdownContent = markdownContent
                )
            },
            onSaved = onSaved
        )
    }

    fun updateModule(
        moduleId: String,
        title: String,
        description: String,
        contentType: String,
        markdownContent: String,
        onSaved: () -> Unit = {}
    ) {
        mutateModule(
            validateTitle = title,
            action = {
                subjectsRepository.updateModule(
                    moduleId = moduleId,
                    title = title,
                    description = description,
                    contentType = contentType,
                    markdownContent = markdownContent
                )
            },
            onSaved = onSaved
        )
    }

    fun deleteModule(
        moduleId: String,
        onDeleted: () -> Unit = {}
    ) {
        mutateModule(
            validateTitle = "delete",
            action = { subjectsRepository.deleteModule(moduleId) },
            onSaved = onDeleted
        )
    }

    private fun mutateModule(
        validateTitle: String,
        action: suspend () -> Result<Unit>,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            if (validateTitle.trim().isBlank()) {
                _uiState.update { it.copy(errorMessage = "Module title is required.") }
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
                refreshOverview(showRefreshing = true)
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

    private suspend fun refreshOverview(showRefreshing: Boolean = true) {
        val hasContent = _uiState.value.overview != null
        _uiState.update {
            it.copy(
                isLoading = !hasContent,
                isRefreshing = showRefreshing && hasContent,
                errorMessage = null
            )
        }

        val overviewResult = subjectsRepository.getSubjectOverview(subjectId)
        val modulesResult = subjectsRepository.getSubjectModules(subjectId)
        val overview = overviewResult.getOrNull()

        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                isMutating = false,
                overview = overview ?: it.overview,
                modules = modulesResult.getOrNull()
                    ?: overview?.latestModules
                    ?: it.modules,
                errorMessage = overviewResult.exceptionOrNull()?.message
                    ?: modulesResult.exceptionOrNull()?.message
            )
        }
    }
}
