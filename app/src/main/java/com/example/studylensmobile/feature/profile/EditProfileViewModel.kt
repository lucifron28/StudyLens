package com.example.studylensmobile.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.AuthRepository
import com.example.studylensmobile.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val result = authRepository.getCurrentUser()
            result.getOrNull()?.let { user ->
                _uiState.update {
                    it.copy(
                        username = user.username,
                        firstName = user.firstName,
                        lastName = user.lastName
                    )
                }
            }
        }
    }

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun updateFirstName(value: String) {
        _uiState.update { it.copy(firstName = value, errorMessage = null) }
    }

    fun updateLastName(value: String) {
        _uiState.update { it.copy(lastName = value, errorMessage = null) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val state = _uiState.value
            val result = authRepository.updateProfile(
                username = state.username,
                firstName = state.firstName,
                lastName = state.lastName
            )
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = result.exceptionOrNull()?.message ?: "An error occurred."
                    )
                }
            }
        }
    }
}
