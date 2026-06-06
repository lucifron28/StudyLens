package com.example.modulelensmobile.feature.scans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modulelensmobile.data.repository.BoardScansRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OcrResultViewModel(
    private val scanId: String,
    private val boardScansRepository: BoardScansRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OcrResultUiState())
    val uiState: StateFlow<OcrResultUiState> = _uiState.asStateFlow()

    init {
        loadScan()
    }

    fun loadScan() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    saveMessage = null
                )
            }

            val result = boardScansRepository.getBoardScan(scanId)

            _uiState.update {
                val scan = result.getOrNull()
                it.copy(
                    isLoading = false,
                    boardScan = scan ?: it.boardScan,
                    editedCleanedText = scan?.cleanedText?.ifBlank { scan.rawOcrText }
                        ?: it.editedCleanedText,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun updateCleanedText(text: String) {
        _uiState.update {
            it.copy(
                editedCleanedText = text,
                saveMessage = null
            )
        }
    }

    fun saveNote(onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val text = _uiState.value.editedCleanedText.trim()
            if (text.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Cleaned OCR text cannot be empty.") }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    saveMessage = null
                )
            }

            val result = boardScansRepository.updateBoardScan(
                scanId = scanId,
                cleanedText = text,
                reviewStatus = "reviewed"
            )

            _uiState.update {
                val scan = result.getOrNull()
                it.copy(
                    isSaving = false,
                    boardScan = scan ?: it.boardScan,
                    editedCleanedText = scan?.cleanedText ?: it.editedCleanedText,
                    errorMessage = result.exceptionOrNull()?.message,
                    saveMessage = if (result.isSuccess) "Note saved." else null
                )
            }

            if (result.isSuccess) {
                onSaved()
            }
        }
    }
}
