package com.example.modulelensmobile.feature.scans

import com.example.modulelensmobile.domain.model.BoardScan

data class BoardNotesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val boardScans: List<BoardScan> = emptyList(),
    val errorMessage: String? = null
)
