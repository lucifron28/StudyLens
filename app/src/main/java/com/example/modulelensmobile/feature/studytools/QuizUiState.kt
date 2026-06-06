package com.example.modulelensmobile.feature.studytools

import com.example.modulelensmobile.domain.model.Quiz

data class QuizUiState(
    val isLoading: Boolean = true,
    val quiz: Quiz? = null,
    val errorMessage: String? = null
)
