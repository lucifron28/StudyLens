package com.example.studylensmobile.feature.subjects

import com.example.studylensmobile.domain.model.SubjectOverview
import com.example.studylensmobile.domain.model.SubjectModulePreview

data class SubjectDetailUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isMutating: Boolean = false,
    val overview: SubjectOverview? = null,
    val modules: List<SubjectModulePreview> = emptyList(),
    val errorMessage: String? = null
)
