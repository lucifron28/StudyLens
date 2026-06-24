package com.example.studylensmobile.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
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
}
