package com.example.studylensmobile.feature.subjects

import com.example.studylensmobile.domain.model.SubjectOverview

data class SubjectDetailUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val overview: SubjectOverview? = null,
    val errorMessage: String? = null
)
