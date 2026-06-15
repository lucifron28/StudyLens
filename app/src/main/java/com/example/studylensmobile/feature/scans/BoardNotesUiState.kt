package com.example.studylensmobile.feature.scans

import com.example.studylensmobile.domain.model.BoardScan

data class BoardNotesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isMutating: Boolean = false,
    val searchQuery: String = "",
    val boardScans: List<BoardScan> = emptyList(),
    val errorMessage: String? = null
)
