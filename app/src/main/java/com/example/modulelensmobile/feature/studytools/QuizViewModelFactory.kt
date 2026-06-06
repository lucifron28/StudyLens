package com.example.modulelensmobile.feature.studytools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.modulelensmobile.data.repository.AiRepository

class QuizViewModelFactory(
    private val sourceType: String,
    private val sourceId: String,
    private val aiRepository: AiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(sourceType, sourceId, aiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
