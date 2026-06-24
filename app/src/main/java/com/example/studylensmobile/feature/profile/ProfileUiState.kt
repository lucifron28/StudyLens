package com.example.studylensmobile.feature.profile

import com.example.studylensmobile.domain.model.User
import com.example.studylensmobile.core.datastore.ThemeOption

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isUploadingImage: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val themeOption: ThemeOption = ThemeOption.SYSTEM
)
