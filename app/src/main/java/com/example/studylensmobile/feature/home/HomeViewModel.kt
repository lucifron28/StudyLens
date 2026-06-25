package com.example.studylensmobile.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.AuthRepository
import com.example.studylensmobile.data.repository.DashboardRepository
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
    fun toggleTaskCompletion(taskId: Int) {
        val currentDashboard = _uiState.value.dashboard ?: return
        val task = currentDashboard.upcoming.find { it.id == taskId } ?: return
        val newStatus = !task.isCompleted

        // Optimistically update UI: filter out completed tasks from the dashboard view
        val updatedUpcoming = if (newStatus) {
            currentDashboard.upcoming.filterNot { it.id == taskId }
        } else {
            currentDashboard.upcoming.map {
                if (it.id == taskId) it.copy(isCompleted = newStatus) else it
            }
        }
        val pendingDelta = if (newStatus) -1 else 1
        val updatedStats = currentDashboard.stats.copy(
            pendingTasks = maxOf(0, currentDashboard.stats.pendingTasks + pendingDelta)
        )
        _uiState.update {
            it.copy(
                dashboard = currentDashboard.copy(
                    upcoming = updatedUpcoming,
                    stats = updatedStats
                )
            )
        }

        viewModelScope.launch {
            val result = dashboardRepository.toggleTaskCompletion(taskId, newStatus)
            if (result.isFailure) {
                // Revert on failure
                loadDashboard()
            }
        }
    }
}

