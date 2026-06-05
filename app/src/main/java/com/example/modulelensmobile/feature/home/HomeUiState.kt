package com.example.modulelensmobile.feature.home

import com.example.modulelensmobile.domain.model.Dashboard
import com.example.modulelensmobile.domain.model.User

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val user: User? = null,
    val dashboard: Dashboard? = null,
    val errorMessage: String? = null
)

