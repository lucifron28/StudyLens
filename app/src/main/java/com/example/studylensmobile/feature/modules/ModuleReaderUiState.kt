package com.example.studylensmobile.feature.modules

import com.example.studylensmobile.domain.model.LearningModule

data class ModuleReaderUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isMutating: Boolean = false,
    val module: LearningModule? = null,
    val errorMessage: String? = null
)
