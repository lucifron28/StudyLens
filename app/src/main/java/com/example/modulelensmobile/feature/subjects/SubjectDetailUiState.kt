package com.example.modulelensmobile.feature.subjects

import com.example.modulelensmobile.domain.model.SubjectOverview

data class SubjectDetailUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val overview: SubjectOverview? = null,
    val errorMessage: String? = null
)
