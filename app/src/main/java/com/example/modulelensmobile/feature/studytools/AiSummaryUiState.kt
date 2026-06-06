package com.example.modulelensmobile.feature.studytools

import com.example.modulelensmobile.domain.model.Summary

data class AiSummaryUiState(
    val isLoading: Boolean = true,
    val summary: Summary? = null,
    val errorMessage: String? = null
)
