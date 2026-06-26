package com.example.studylensmobile.feature.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.ModulesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ModuleReaderViewModel(
    private val moduleId: String,
    private val modulesRepository: ModulesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ModuleReaderUiState())
    val uiState: StateFlow<ModuleReaderUiState> = _uiState.asStateFlow()

    init {
        loadModule()
    }

    fun loadModule() {
        viewModelScope.launch {
            val hasContent = _uiState.value.module != null
            _uiState.update {
                it.copy(
                    isLoading = !hasContent,
                    isRefreshing = hasContent,
                    errorMessage = null
                )
            }

            val result = modulesRepository.getModuleReader(moduleId)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    module = result.getOrNull() ?: it.module,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun updateModule(
        title: String,
        description: String,
        contentType: String,
        markdownContent: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMutating = true) }
            val result = modulesRepository.updateModule(
                moduleId = moduleId,
                title = title,
                description = description,
                contentType = contentType,
                markdownContent = markdownContent
            )
            _uiState.update {
                it.copy(
                    isMutating = false,
                    module = result.getOrNull() ?: it.module,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            if (result.isSuccess) {
                onSaved()
            }
        }
    }
}
