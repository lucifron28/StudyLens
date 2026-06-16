package com.example.studylensmobile.feature.scans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.domain.model.BoardScan
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.StatusChip

@Composable
fun OcrResultScreen(
    viewModel: OcrResultViewModel,
    onBack: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    onNavigateToFlashcards: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToTutor: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scan = uiState.boardScan

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = "OCR Result",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadScan) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh OCR result"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                StudyLensLoadingState(
                    message = "Loading OCR result...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            scan == null -> {
                StudyLensErrorState(
                    message = uiState.errorMessage ?: "OCR result is unavailable.",
                    onRetry = viewModel::loadScan,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            else -> {
                OcrResultContent(
                    scan = scan,
                    editedCleanedText = uiState.editedCleanedText,
                    isSaving = uiState.isSaving,
                    errorMessage = uiState.errorMessage,
                    saveMessage = uiState.saveMessage,
                    onCleanedTextChange = viewModel::updateCleanedText,
                    onSaveNote = { viewModel.saveNote() },
                    onNavigateToSummary = onNavigateToSummary,
                    onNavigateToFlashcards = onNavigateToFlashcards,
                    onNavigateToQuiz = onNavigateToQuiz,
                    onNavigateToTutor = onNavigateToTutor,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
@Composable
private fun OcrResultContent(
    scan: BoardScan,
    editedCleanedText: String,
    isSaving: Boolean,
    errorMessage: String?,
    saveMessage: String?,
    onCleanedTextChange: (String) -> Unit,
    onSaveNote: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    onNavigateToFlashcards: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToTutor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeaderCard(scan = scan)

        if (errorMessage != null) {
            StudyLensCard {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        OcrTextEditorCard(
            title = "Extracted Text",
            text = editedCleanedText,
            isSaving = isSaving,
            onTextChange = onCleanedTextChange
        )

        FilingDetailsCard(scan = scan)

        if (saveMessage != null) {
            Text(
                text = saveMessage,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onSaveNote,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSaving) "Saving..." else "Save Note")
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onNavigateToSummary(scan.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Summary")
                }
                Button(
                    onClick = { onNavigateToFlashcards(scan.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cards")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onNavigateToQuiz(scan.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Quiz")
                }
                Button(
                    onClick = { onNavigateToTutor(scan.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tutor")
                }
            }
        }
    }
}
@Composable
private fun HeaderCard(scan: BoardScan) {
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
                    text = scan.title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = scan.reviewStatus)
            }
            Text(
                text = "Captured ${scan.createdAt}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp)
            )
            if (scan.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scan.tags.take(3).forEach { tag ->
                        StatusChip(status = "#${tag.name}")
                    }
                }
            }
        }
    }
}

@Composable
private fun OcrTextEditorCard(
    title: String,
    text: String,
    isSaving: Boolean,
    onTextChange: (String) -> Unit
) {
    StudyLensCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                minLines = 6,
                placeholder = { Text("Cleaned OCR text") }
            )
        }
    }
}

@Composable
private fun FilingDetailsCard(scan: BoardScan) {
    StudyLensCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filing Details",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DetailLine(label = "Subject", value = scan.subjectTitle.ifBlank { "Not linked" })
            DetailLine(label = "Module", value = scan.moduleTitle.ifBlank { "Not linked" })
            DetailLine(label = "Chapter", value = scan.chapterTitle.ifBlank { "Not linked" })
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
