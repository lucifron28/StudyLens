package com.example.studylensmobile.feature.studytools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
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
import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.domain.model.Summary
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensInlineError
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.SectionHeader
import com.example.studylensmobile.ui.components.StatusChip

@Composable
fun AiSummaryScreen(
    viewModel: AiSummaryViewModel,
    onBack: () -> Unit,
    onCreateFlashcards: (String, String) -> Unit,
    onPracticeQuiz: (String, String) -> Unit,
    onAskTutor: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = "AI Summary",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::generateSummary) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate summary"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                StudyLensLoadingState(
                    message = "Generating summary...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            summary == null -> {
                StudyLensErrorState(
                    message = uiState.errorMessage ?: "Summary is unavailable.",
                    onRetry = viewModel::generateSummary,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            else -> {
                SummaryContent(
                    summary = summary,
                    errorMessage = uiState.errorMessage,
                    onRetry = viewModel::generateSummary,
                    onCreateFlashcards = onCreateFlashcards,
                    onPracticeQuiz = onPracticeQuiz,
                    onAskTutor = onAskTutor,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun SummaryContent(
    summary: Summary,
    errorMessage: String?,
    onRetry: () -> Unit,
    onCreateFlashcards: (String, String) -> Unit,
    onPracticeQuiz: (String, String) -> Unit,
    onAskTutor: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sourceId = summary.sourceIdForActions()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (errorMessage != null) {
            StudyLensInlineError(message = errorMessage, onRetry = onRetry)
        }

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
                        text = "Key Summary",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    StatusChip(status = summary.sourceType.toDisplayLabel())
                }
                if (summary.title.isNotBlank()) {
                    Text(
                        text = summary.title,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                Text(
                    text = summary.content,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        if (summary.keyTakeaways.isNotEmpty()) {
            SectionHeader(title = "Key Takeaways")
            summary.keyTakeaways.forEach { takeaway ->
                StudyLensCard {
                    Text(
                        text = takeaway,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (sourceId != null) {
            SectionHeader(title = "Next Actions")
            Button(
                onClick = { onCreateFlashcards(summary.sourceType, sourceId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Flashcards")
            }
            Button(
                onClick = { onPracticeQuiz(summary.sourceType, sourceId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Practice Quiz")
            }
            Button(
                onClick = { onAskTutor(summary.sourceType, sourceId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ask AI Tutor")
            }
        }
    }
}

private fun Summary.sourceIdForActions(): String? {
    return when (sourceType) {
        "module" -> moduleId
        "chapter" -> chapterId
        "board_scan" -> boardScanId
        else -> null
    }
}
