package com.example.studylensmobile.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.AuthRepository
import com.example.studylensmobile.core.datastore.ThemePreferences
import com.example.studylensmobile.core.datastore.ThemeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeTheme()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreferences.themeOption.collect { option ->
                _uiState.update { it.copy(themeOption = option) }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            val hasProfile = _uiState.value.user != null
            _uiState.update {
                it.copy(
                    isLoading = !hasProfile,
                    isRefreshing = hasProfile,
                    errorMessage = null
                )
            }

            val result = authRepository.getCurrentUser()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    user = result.getOrNull() ?: it.user,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun uploadProfileImage(file: java.io.File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true, errorMessage = null) }
            val result = authRepository.uploadProfileImage(file)
            _uiState.update {
                it.copy(
                    isUploadingImage = false,
                    user = result.getOrNull() ?: it.user,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun setThemeOption(option: ThemeOption) {
        viewModelScope.launch {
            themePreferences.setThemeOption(option)
        }
    }
}
