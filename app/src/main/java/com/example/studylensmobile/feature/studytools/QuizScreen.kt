package com.example.studylensmobile.feature.studytools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.domain.model.Quiz
import com.example.studylensmobile.domain.model.QuizQuestion
import com.example.studylensmobile.ui.components.MarkdownInlineText
import com.example.studylensmobile.ui.components.MarkdownText
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensEmptyState
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensInlineError
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.StatusChip

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val quiz = uiState.quiz

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = "Practice Quiz",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::generateQuiz) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate quiz"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                StudyLensLoadingState(
                    message = "Generating quiz...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            quiz == null -> {
                StudyLensErrorState(
                    message = uiState.errorMessage ?: "Quiz is unavailable.",
                    onRetry = viewModel::generateQuiz,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            else -> {
                QuizContent(
                    uiState = uiState,
                    quiz = quiz,
                    errorMessage = uiState.errorMessage,
                    onCountSelected = viewModel::setRequestedCount,
                    onRetry = viewModel::generateQuiz,
                    onAnswerSelected = viewModel::selectAnswer,
                    onSubmit = viewModel::submitCurrentAnswer,
                    onPrevious = viewModel::previousQuestion,
                    onNext = viewModel::nextQuestion,
                    onRestart = viewModel::restartQuiz,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun QuizContent(
    uiState: QuizUiState,
    quiz: Quiz,
    errorMessage: String?,
    onCountSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    onAnswerSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            QuizHeader(
                quiz = quiz,
                score = uiState.score,
                submittedCount = uiState.submittedCount,
                isComplete = uiState.isComplete
            )
        }

        if (errorMessage != null) {
            item {
                StudyLensInlineError(message = errorMessage, onRetry = onRetry)
            }
        }

        item {
            QuizCountSelector(
                selectedCount = uiState.requestedCount,
                onCountSelected = onCountSelected,
                onGenerate = onRetry
            )
        }

        val question = uiState.currentQuestion
        if (question == null) {
            item {
                StudyLensEmptyState(text = "No quiz questions generated yet.")
            }
        } else {
            item(key = "question-${question.id}") {
                QuizQuestionPracticeCard(
                    question = question,
                    progressLabel = uiState.progressLabel,
                    selectedAnswer = uiState.selectedAnswer,
                    isSubmitted = uiState.isCurrentSubmitted,
                    onAnswerSelected = onAnswerSelected,
                    onSubmit = onSubmit
                )
            }

            item {
                QuizNavigationControls(
                    canGoPrevious = uiState.canGoPrevious,
                    canGoNext = uiState.canGoNext,
                    isComplete = uiState.isComplete,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onRestart = onRestart
                )
            }
        }
    }
}

@Composable
private fun QuizHeader(
    quiz: Quiz,
    score: Int,
    submittedCount: Int,
    isComplete: Boolean
) {
    StudyLensCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = quiz.title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = "$score / ${quiz.questions.size}")
            }
            if (quiz.description.isNotBlank()) {
                Text(
                    text = quiz.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Text(
                text = if (isComplete) "Quiz complete" else "$submittedCount answered",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun QuizCountSelector(
    selectedCount: Int,
    onCountSelected: (Int) -> Unit,
    onGenerate: () -> Unit
) {
    StudyLensCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Quiz Size",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 10.dp)
            ) {
                listOf(3, 5, 8).forEach { count ->
                    FilterChip(
                        selected = selectedCount == count,
                        onClick = { onCountSelected(count) },
                        label = { Text("$count items") }
                    )
                }
            }
            Button(
                onClick = onGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Generate Quiz")
            }
        }
    }
}

@Composable
private fun QuizQuestionPracticeCard(
    question: QuizQuestion,
    progressLabel: String,
    selectedAnswer: String,
    isSubmitted: Boolean,
    onAnswerSelected: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val isCorrect = selectedAnswer.matchesAnswer(question.correctAnswer)

    StudyLensCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Question ${question.order}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(status = question.questionType)
                    StatusChip(status = progressLabel)
                }
            }

            MarkdownText(
                markdown = question.question,
                color = MaterialTheme.colorScheme.onSurface,
                bodyStyle = MaterialTheme.typography.bodyLarge
            )

            AnswerInput(
                question = question,
                selectedAnswer = selectedAnswer,
                isSubmitted = isSubmitted,
                onAnswerSelected = onAnswerSelected
            )

            if (isSubmitted) {
                AnswerFeedback(
                    isCorrect = isCorrect,
                    correctAnswer = question.correctAnswer,
                    explanation = question.explanation
                )
            } else {
                Button(
                    onClick = onSubmit,
                    enabled = selectedAnswer.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Answer")
                }
            }
        }
    }
}

@Composable
private fun AnswerInput(
    question: QuizQuestion,
    selectedAnswer: String,
    isSubmitted: Boolean,
    onAnswerSelected: (String) -> Unit
) {
    if (question.isShortAnswer()) {
        OutlinedTextField(
            value = selectedAnswer,
            onValueChange = onAnswerSelected,
            enabled = !isSubmitted,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            label = { Text("Your answer") }
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            question.answerChoices().forEach { choice ->
                val isSelected = selectedAnswer == choice
                if (isSelected) {
                    FilledTonalButton(
                        onClick = { onAnswerSelected(choice) },
                        enabled = !isSubmitted,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MarkdownInlineText(
                            markdown = choice,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { onAnswerSelected(choice) },
                        enabled = !isSubmitted,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MarkdownInlineText(
                            markdown = choice,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerFeedback(
    isCorrect: Boolean,
    correctAnswer: String,
    explanation: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isCorrect) "Correct" else "Needs review",
            color = if (isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        MarkdownInlineText(
            markdown = "**Answer:** $correctAnswer",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (explanation.isNotBlank()) {
            MarkdownText(
                markdown = explanation,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                bodyStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun QuizNavigationControls(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    isComplete: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = canGoPrevious,
            modifier = Modifier.weight(1f)
        ) {
            Text("Previous")
        }
        Button(
            onClick = if (isComplete && !canGoNext) onRestart else onNext,
            enabled = canGoNext || isComplete,
            modifier = Modifier.weight(1f)
        ) {
            Text(if (isComplete && !canGoNext) "Restart" else "Next")
        }
    }
}

private fun QuizQuestion.answerChoices(): List<String> {
    if (choices.isNotEmpty()) {
        return choices
    }

    return if (questionType.equals("True False", ignoreCase = true)) {
        listOf("True", "False")
    } else {
        emptyList()
    }
}

private fun QuizQuestion.isShortAnswer(): Boolean {
    return questionType.equals("Short Answer", ignoreCase = true) || answerChoices().isEmpty()
}
