package com.example.modulelensmobile.feature.studytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.modulelensmobile.data.repository.AiRepository

class AiSummaryViewModelFactory(
    private val sourceType: String,
    private val sourceId: String,
    private val aiRepository: AiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiSummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AiSummaryViewModel(sourceType, sourceId, aiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
