package com.example.modulelensmobile.feature.modules

import com.example.modulelensmobile.domain.model.LearningModule

data class ModuleReaderUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val module: LearningModule? = null,
    val errorMessage: String? = null
)
