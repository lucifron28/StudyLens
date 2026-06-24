package com.example.studylensmobile.feature.profile

data class EditProfileUiState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
