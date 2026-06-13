package com.example.modulelensmobile.feature.studytools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modulelensmobile.domain.model.Quiz
import com.example.modulelensmobile.domain.model.QuizQuestion
import com.example.modulelensmobile.ui.components.ModuleLensCard
import com.example.modulelensmobile.ui.components.ModuleLensEmptyState
import com.example.modulelensmobile.ui.components.ModuleLensErrorState
import com.example.modulelensmobile.ui.components.ModuleLensInlineError
import com.example.modulelensmobile.ui.components.ModuleLensLoadingState
import com.example.modulelensmobile.ui.components.ModuleLensTopBar
import com.example.modulelensmobile.ui.components.StatusChip

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val quiz = uiState.quiz

    Scaffold(
        topBar = {
            ModuleLensTopBar(
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
                ModuleLensLoadingState(
                    message = "Generating quiz...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            quiz == null -> {
                ModuleLensErrorState(
                    message = uiState.errorMessage ?: "Quiz is unavailable.",
                    onRetry = viewModel::generateQuiz,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            else -> {
                QuizContent(
                    quiz = quiz,
                    errorMessage = uiState.errorMessage,
                    onRetry = viewModel::generateQuiz,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun QuizContent(
    quiz: Quiz,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            QuizHeader(quiz = quiz)
        }

        if (errorMessage != null) {
            item {
                ModuleLensInlineError(message = errorMessage, onRetry = onRetry)
            }
        }

        if (quiz.questions.isEmpty()) {
            item {
                ModuleLensEmptyState(text = "No quiz questions generated yet.")
            }
        } else {
            items(quiz.questions, key = { it.id }) { question ->
                QuizQuestionCard(question = question)
            }
        }
    }
}

@Composable
private fun QuizHeader(quiz: Quiz) {
    ModuleLensCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quiz.title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = "${quiz.questions.size} items")
            }
            if (quiz.description.isNotBlank()) {
                Text(
                    text = quiz.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun QuizQuestionCard(question: QuizQuestion) {
    ModuleLensCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${question.order}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(status = question.questionType)
            }
            Text(
                text = question.question,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 10.dp)
            )
            if (question.choices.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    question.choices.forEachIndexed { index, choice ->
                        Text(
                            text = "${index + 1}. $choice",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            Text(
                text = "Answer: ${question.correctAnswer}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
            if (question.explanation.isNotBlank()) {
                Text(
                    text = question.explanation,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
