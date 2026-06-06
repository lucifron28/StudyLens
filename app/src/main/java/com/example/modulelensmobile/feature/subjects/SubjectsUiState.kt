package com.example.modulelensmobile.feature.subjects

import com.example.modulelensmobile.domain.model.Subject

data class SubjectsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val subjects: List<Subject> = emptyList(),
    val errorMessage: String? = null
)
