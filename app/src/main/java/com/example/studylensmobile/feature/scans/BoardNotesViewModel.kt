package com.example.studylensmobile.feature.scans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.data.repository.BoardScansRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BoardNotesViewModel(
    private val boardScansRepository: BoardScansRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BoardNotesUiState())
    val uiState: StateFlow<BoardNotesUiState> = _uiState.asStateFlow()

    init {
        loadBoardScans()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadBoardScans() {
        viewModelScope.launch {
            val hasContent = _uiState.value.boardScans.isNotEmpty()
            _uiState.update {
                it.copy(
                    isLoading = !hasContent,
                    isRefreshing = hasContent,
                    errorMessage = null
                )
            }

            val result = boardScansRepository.getBoardScans(
                search = _uiState.value.searchQuery
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    boardScans = result.getOrNull() ?: it.boardScans,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
}
