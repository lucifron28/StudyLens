package com.example.studylensmobile.feature.home

import com.example.studylensmobile.domain.model.Dashboard
import com.example.studylensmobile.domain.model.User

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val user: User? = null,
    val dashboard: Dashboard? = null,
    val errorMessage: String? = null
)

