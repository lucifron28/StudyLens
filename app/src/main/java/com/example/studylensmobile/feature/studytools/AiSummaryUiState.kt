package com.example.studylensmobile.feature.studytools

import com.example.studylensmobile.domain.model.Summary

data class AiSummaryUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val summary: Summary? = null,
    val errorMessage: String? = null
)
