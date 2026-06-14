package com.example.studylensmobile.feature.scans

import com.example.studylensmobile.domain.model.BoardScan

data class OcrResultUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val boardScan: BoardScan? = null,
    val editedCleanedText: String = "",
    val errorMessage: String? = null,
    val saveMessage: String? = null
)
