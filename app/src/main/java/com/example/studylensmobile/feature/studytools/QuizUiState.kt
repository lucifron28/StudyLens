package com.example.studylensmobile.feature.studytools

import com.example.studylensmobile.domain.model.Quiz

data class QuizUiState(
    val isLoading: Boolean = true,
    val quiz: Quiz? = null,
    val errorMessage: String? = null
)
