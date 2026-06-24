package com.example.studylensmobile.feature.scans

import com.example.studylensmobile.domain.model.BoardScan
import com.example.studylensmobile.domain.model.LearningChapter
import com.example.studylensmobile.domain.model.Subject
import com.example.studylensmobile.domain.model.SubjectModulePreview

data class BoardNotesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isMutating: Boolean = false,
    val isRecognizingText: Boolean = false,
    val ocrDraftText: String = "",
    val pendingImageUri: String? = null,
    val searchQuery: String = "",
    val boardScans: List<BoardScan> = emptyList(),
    val errorMessage: String? = null,
    // Dropdown data for the form
    val availableSubjects: List<Subject> = emptyList(),
    val availableModules: List<SubjectModulePreview> = emptyList(),
    val availableChapters: List<LearningChapter> = emptyList(),
    val isLoadingSubjects: Boolean = false,
    val isLoadingModules: Boolean = false,
    val isLoadingChapters: Boolean = false,
)
