package com.example.studylensmobile.feature.studytools

import com.example.studylensmobile.domain.model.Quiz
import com.example.studylensmobile.domain.model.QuizQuestion

data class QuizUiState(
    val isLoading: Boolean = true,
    val quiz: Quiz? = null,
    val currentIndex: Int = 0,
    val selectedAnswers: Map<String, String> = emptyMap(),
    val submittedQuestionIds: Set<String> = emptySet(),
    val requestedCount: Int = 5,
    val errorMessage: String? = null
) {
    val currentQuestion: QuizQuestion?
        get() = quiz?.questions?.getOrNull(currentIndex)

    val selectedAnswer: String
        get() = currentQuestion?.let { selectedAnswers[it.id] }.orEmpty()

    val isCurrentSubmitted: Boolean
        get() = currentQuestion?.id in submittedQuestionIds

    val canGoPrevious: Boolean
        get() = currentIndex > 0

    val canGoNext: Boolean
        get() = currentIndex < ((quiz?.questions?.lastIndex) ?: 0)

    val score: Int
        get() = quiz?.questions.orEmpty()
            .count { question ->
                question.id in submittedQuestionIds &&
                    selectedAnswers[question.id].orEmpty().matchesAnswer(question.correctAnswer)
            }

    val submittedCount: Int
        get() = submittedQuestionIds.size

    val isComplete: Boolean
        get() {
            val questionCount = quiz?.questions?.size ?: 0
            return questionCount > 0 && submittedCount == questionCount
        }

    val progressLabel: String
        get() {
            val total = quiz?.questions?.size ?: 0
            return if (total == 0) "0 / 0" else "${currentIndex + 1} / $total"
        }
}

fun String.matchesAnswer(correctAnswer: String): Boolean {
    return trim().equals(correctAnswer.trim(), ignoreCase = true)
}
