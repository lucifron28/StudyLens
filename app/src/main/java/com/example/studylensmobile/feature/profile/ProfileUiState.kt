package com.example.studylensmobile.feature.profile

import com.example.studylensmobile.domain.model.User

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)
