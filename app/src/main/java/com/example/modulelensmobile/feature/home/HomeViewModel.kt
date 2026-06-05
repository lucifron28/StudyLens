package com.example.modulelensmobile.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modulelensmobile.data.repository.AuthRepository
import com.example.modulelensmobile.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dashboardRepository: DashboardRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            val hasContent = _uiState.value.dashboard != null
            _uiState.update {
                it.copy(
                    isLoading = !hasContent,
                    isRefreshing = hasContent,
                    errorMessage = null
                )
            }

            val userResult = authRepository.getCurrentUser()
            val dashboardResult = dashboardRepository.getDashboard()

            val user = userResult.getOrNull()
            val dashboard = dashboardResult.getOrNull()
            val error = userResult.exceptionOrNull()?.message
                ?: dashboardResult.exceptionOrNull()?.message

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    user = user ?: it.user,
                    dashboard = dashboard ?: it.dashboard,
                    errorMessage = error
                )
            }
        }
    }
}

