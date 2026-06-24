package com.example.studylensmobile.feature.scans

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studylensmobile.core.ocr.OcrTextRecognizer
import com.example.studylensmobile.data.repository.BoardScansRepository
import com.example.studylensmobile.data.repository.ModulesRepository
import com.example.studylensmobile.data.repository.SubjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BoardNotesViewModel(
    private val boardScansRepository: BoardScansRepository,
    private val subjectsRepository: SubjectsRepository,
    private val modulesRepository: ModulesRepository
) : ViewModel() {
    private var modulesRequestVersion = 0
    private var chaptersRequestVersion = 0

    private val _uiState = MutableStateFlow(BoardNotesUiState())
    val uiState: StateFlow<BoardNotesUiState> = _uiState.asStateFlow()

    init {
        loadBoardScans()
        loadSubjects()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadBoardScans() {
        viewModelScope.launch {
            refreshBoardScans()
        }
    }

    fun loadSubjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSubjects = true) }
            val result = subjectsRepository.getSubjects()
            _uiState.update {
                it.copy(
                    availableSubjects = result.getOrNull() ?: it.availableSubjects,
                    isLoadingSubjects = false
                )
            }
        }
    }

    fun fetchModulesForSubject(subjectId: String) {
        val requestVersion = ++modulesRequestVersion
        chaptersRequestVersion++
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingModules = true,
                    isLoadingChapters = false,
                    availableModules = emptyList(),
                    availableChapters = emptyList()
                )
            }
            val result = subjectsRepository.getSubjectModules(subjectId)
            if (requestVersion != modulesRequestVersion) return@launch

            _uiState.update {
                it.copy(
                    availableModules = result.getOrNull() ?: emptyList(),
                    isLoadingModules = false
                )
            }
        }
    }

    fun fetchChaptersForModule(moduleId: String) {
        val requestVersion = ++chaptersRequestVersion
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingChapters = true,
                    availableChapters = emptyList()
                )
            }
            val result = modulesRepository.getModuleReader(moduleId)
            if (requestVersion != chaptersRequestVersion) return@launch

            _uiState.update {
                it.copy(
                    availableChapters = result.getOrNull()?.chapters ?: emptyList(),
                    isLoadingChapters = false
                )
            }
        }
    }

    fun clearModulesAndChapters() {
        modulesRequestVersion++
        chaptersRequestVersion++
        _uiState.update {
            it.copy(
                availableModules = emptyList(),
                availableChapters = emptyList(),
                isLoadingModules = false,
                isLoadingChapters = false
            )
        }
    }

    fun recognizeBoardImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRecognizingText = true,
                    pendingImageUri = imageUri.toString(),
                    errorMessage = null
                )
            }

            runCatching {
                OcrTextRecognizer.recognizeText(context.applicationContext, imageUri)
            }.fold(
                onSuccess = { text ->
                    _uiState.update {
                        it.copy(
                            isRecognizingText = false,
                            ocrDraftText = text,
                            errorMessage = if (text.isBlank()) {
                                "No text was detected in the selected image."
                            } else {
                                null
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isRecognizingText = false,
                            errorMessage = error.message ?: "OCR failed. Try another image."
                        )
                    }
                }
            )
        }
    }

    fun clearOcrDraft() {
        _uiState.update { it.copy(ocrDraftText = "", isRecognizingText = false) }
    }

    fun discardPendingImage() {
        _uiState.update { it.copy(ocrDraftText = "", pendingImageUri = null, isRecognizingText = false) }
    }

    fun createBoardScan(
        rawOcrText: String,
        cleanedText: String,
        summary: String,
        reviewStatus: String,
        subjectId: String?,
        moduleId: String?,
        chapterId: String?,
        imageUri: String?,
        onSaved: () -> Unit = {}
    ) {
        mutateBoardScan(
            validateText = cleanedText.ifBlank { rawOcrText },
            action = {
                boardScansRepository.createBoardScan(
                    rawOcrText = rawOcrText,
                    cleanedText = cleanedText,
                    summary = summary,
                    reviewStatus = reviewStatus,
                    subjectId = subjectId,
                    moduleId = moduleId,
                    chapterId = chapterId,
                    imageUri = imageUri
                )
            },
            onSaved = onSaved
        )
    }

    fun updateBoardScan(
        scanId: String,
        rawOcrText: String,
        cleanedText: String,
        summary: String,
        reviewStatus: String,
        subjectId: String?,
        moduleId: String?,
        chapterId: String?,
        imageUri: String?,
        onSaved: () -> Unit = {}
    ) {
        mutateBoardScan(
            validateText = cleanedText.ifBlank { rawOcrText },
            action = {
                boardScansRepository.updateBoardScanDetails(
                    scanId = scanId,
                    rawOcrText = rawOcrText,
                    cleanedText = cleanedText,
                    summary = summary,
                    reviewStatus = reviewStatus,
                    subjectId = subjectId,
                    moduleId = moduleId,
                    chapterId = chapterId,
                    imageUri = imageUri
                )
            },
            onSaved = onSaved
        )
    }

    fun deleteBoardScan(
        scanId: String,
        onDeleted: () -> Unit = {}
    ) {
        mutateBoardScan(
            validateText = "delete",
            action = { boardScansRepository.deleteBoardScan(scanId) },
            onSaved = onDeleted
        )
    }

    private fun mutateBoardScan(
        validateText: String,
        action: suspend () -> Result<Unit>,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            if (validateText.trim().isBlank()) {
                _uiState.update { it.copy(errorMessage = "Board note text is required.") }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isMutating = true,
                    errorMessage = null
                )
            }

            val result = action()
            if (result.isSuccess) {
                discardPendingImage()
                onSaved()
                refreshBoardScans(showRefreshing = true)
            } else {
                _uiState.update {
                    it.copy(
                        isMutating = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    private suspend fun refreshBoardScans(showRefreshing: Boolean = true) {
        val hasContent = _uiState.value.boardScans.isNotEmpty()
        _uiState.update {
            it.copy(
                isLoading = !hasContent,
                isRefreshing = showRefreshing && hasContent,
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
                isMutating = false,
                boardScans = result.getOrNull() ?: it.boardScans,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }
}
