package com.example.studylensmobile.feature.scans

import com.example.studylensmobile.domain.model.BoardScan

data class BoardNotesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isMutating: Boolean = false,
    val isRecognizingText: Boolean = false,
    val ocrDraftText: String = "",
    val pendingImageUri: String? = null,
    val searchQuery: String = "",
    val boardScans: List<BoardScan> = emptyList(),
    val errorMessage: String? = null
)
