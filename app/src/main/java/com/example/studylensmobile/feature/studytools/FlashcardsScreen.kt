package com.example.studylensmobile.feature.studytools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.R
import com.example.studylensmobile.domain.model.Flashcard
import com.example.studylensmobile.ui.components.LumiCard
import com.example.studylensmobile.ui.components.MarkdownText
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensEmptyState
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensInlineError
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.StatusChip

@Composable
fun FlashcardsScreen(
    viewModel: FlashcardsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = "Flashcards",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::generateFlashcards) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate flashcards"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                StudyLensLoadingState(
                    message = "Generating flashcards...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            uiState.flashcards.isEmpty() && uiState.errorMessage != null -> {
                StudyLensErrorState(
                    message = uiState.errorMessage ?: "Flashcards are unavailable.",
                    onRetry = viewModel::generateFlashcards,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            else -> {
                FlashcardsContent(
                    uiState = uiState,
                    errorMessage = uiState.errorMessage,
                    onCountSelected = viewModel::setRequestedCount,
                    onRetry = viewModel::generateFlashcards,
                    onToggleAnswer = viewModel::toggleAnswer,
                    onPrevious = viewModel::previousCard,
                    onNext = viewModel::nextCard,
                    onRestart = viewModel::restartDeck,
                    onDone = onBack,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun FlashcardsContent(
    uiState: FlashcardsUiState,
    errorMessage: String?,
    onCountSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    onToggleAnswer: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (errorMessage != null && uiState.flashcards.isNotEmpty()) {
            item {
                StudyLensInlineError(message = errorMessage, onRetry = onRetry)
            }
        }

        item {
            FlashcardCountSelector(
                selectedCount = uiState.requestedCount,
                onCountSelected = onCountSelected,
                onGenerate = onRetry
            )
        }

        val flashcard = uiState.currentFlashcard
        if (flashcard == null) {
            item {
                StudyLensEmptyState(text = "No flashcards generated yet.")
            }
        } else {
            item(key = "flashcard-${flashcard.id}") {
                FlashcardReviewCard(
                    flashcard = flashcard,
                    progressLabel = uiState.progressLabel,
                    isAnswerVisible = uiState.isAnswerVisible,
                    onToggleAnswer = onToggleAnswer
                )
            }

            item {
                FlashcardControls(
                    canGoPrevious = uiState.canGoPrevious,
                    canGoNext = uiState.canGoNext,
                    onPrevious = onPrevious,
                    onNext = onNext
                )
            }

            if (uiState.isComplete) {
                item {
                    LumiCard(
                        title = "Deck complete",
                        message = "Nice work. You reviewed all ${uiState.flashcards.size} cards in this deck.",
                        primaryActionLabel = "Done",
                        onPrimaryAction = onDone,
                        imageResId = R.drawable.lumi_celebrating,
                        imageContentDescription = "Lumi celebrating",
                        secondaryActionLabel = "Try Again",
                        onSecondaryAction = onRestart
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashcardCountSelector(
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
                text = "Deck Size",
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
                        label = { Text("$count cards") }
                    )
                }
            }
            Button(
                onClick = onGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Generate Flashcards")
            }
        }
    }
}

@Composable
private fun FlashcardReviewCard(
    flashcard: Flashcard,
    progressLabel: String,
    isAnswerVisible: Boolean,
    onToggleAnswer: () -> Unit
) {
    StudyLensCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(status = flashcard.difficulty)
                    StatusChip(status = progressLabel)
                }
            }
            MarkdownText(
                markdown = flashcard.question,
                color = MaterialTheme.colorScheme.onSurface,
                bodyStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 14.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            FilledTonalButton(
                onClick = onToggleAnswer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isAnswerVisible) "Hide Answer" else "Reveal Answer")
            }

            if (isAnswerVisible) {
                Text(
                    text = "Answer",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 18.dp)
                )
                MarkdownText(
                    markdown = flashcard.answer,
                    color = MaterialTheme.colorScheme.onSurface,
                    bodyStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun FlashcardControls(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
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
            onClick = onNext,
            enabled = canGoNext,
            modifier = Modifier.weight(1f)
        ) {
            Text("Next")
        }
    }
}
