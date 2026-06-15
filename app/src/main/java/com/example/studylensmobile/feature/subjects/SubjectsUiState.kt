package com.example.studylensmobile.feature.subjects

import com.example.studylensmobile.domain.model.Subject

data class SubjectsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isMutating: Boolean = false,
    val searchQuery: String = "",
    val subjects: List<Subject> = emptyList(),
    val errorMessage: String? = null
)
