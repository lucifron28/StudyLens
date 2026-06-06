package com.example.modulelensmobile.feature.scans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.modulelensmobile.data.repository.BoardScansRepository

class OcrResultViewModelFactory(
    private val scanId: String,
    private val boardScansRepository: BoardScansRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OcrResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OcrResultViewModel(scanId, boardScansRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
