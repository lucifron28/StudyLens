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
    onPracticeQuiz: (String, String) -> Unit
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
                    isRefreshing = uiState.isRefreshing,
                    errorMessage = uiState.errorMessage,
                    onRetry = viewModel::generateSummary,
                    onCreateFlashcards = onCreateFlashcards,
                    onPracticeQuiz = onPracticeQuiz,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun SummaryContent(
    summary: Summary,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onCreateFlashcards: (String, String) -> Unit,
    onPracticeQuiz: (String, String) -> Unit,
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

        SummarySourceCard(summary = summary, isRefreshing = isRefreshing)

        SummaryBodyCard(summary = summary)

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
            SummaryActions(
                onCreateFlashcards = { onCreateFlashcards(summary.sourceType, sourceId) },
                onPracticeQuiz = { onPracticeQuiz(summary.sourceType, sourceId) }
            )
        }
    }
}

@Composable
private fun SummarySourceCard(
    summary: Summary,
    isRefreshing: Boolean
) {
    StudyLensCard {
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
                    text = summary.sourceTitle(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = summary.sourceType.toDisplayLabel())
            }
            Text(
                text = if (isRefreshing) "Refreshing summary..." else "Generated ${summary.createdAt}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SummaryBodyCard(summary: Summary) {
    StudyLensCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Key Summary",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = summary.content,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun SummaryActions(
    onCreateFlashcards: () -> Unit,
    onPracticeQuiz: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCreateFlashcards,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cards")
        }
        OutlinedButton(
            onClick = onPracticeQuiz,
            modifier = Modifier.weight(1f)
        ) {
            Text("Quiz")
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

private fun Summary.sourceTitle(): String {
    return title.ifBlank {
        listOf(moduleTitle, chapterTitle)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
            .ifBlank { sourceType.toDisplayLabel() }
    }
}
