package com.example.studylensmobile.feature.modules

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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.domain.model.LearningChapter
import com.example.studylensmobile.domain.model.LearningModule
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensEmptyState
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.ProgressBar
import com.example.studylensmobile.ui.components.SectionHeader
import com.example.studylensmobile.ui.components.StatusChip

@Composable
fun ModuleReaderScreen(
    viewModel: ModuleReaderViewModel,
    onBack: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val module = uiState.module

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = module?.title ?: "Module Reader",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadModule) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh module"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                StudyLensLoadingState(
                    message = "Loading module...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            module == null -> {
                StudyLensErrorState(
                    message = uiState.errorMessage ?: "Module is unavailable.",
                    onRetry = viewModel::loadModule,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            else -> {
                ModuleReaderContent(
                    module = module,
                    isRefreshing = uiState.isRefreshing,
                    onNavigateToSummary = onNavigateToSummary,
                    onNavigateToFlashcards = onNavigateToFlashcards,
                    onNavigateToQuiz = onNavigateToQuiz,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun ModuleReaderContent(
    module: LearningModule,
    isRefreshing: Boolean,
    onNavigateToSummary: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ModuleHeaderCard(module = module, isRefreshing = isRefreshing)
        }

        item {
            SectionHeader(title = "Module Content")
        }

        item {
            ReaderTextCard(
                title = module.title,
                text = module.readerText().ifBlank { "No readable content yet." }
            )
        }

        item {
            SectionHeader(title = "Chapters")
        }

        if (module.chapters.isEmpty()) {
            item {
                StudyLensEmptyState(text = "No chapters added yet.")
            }
        } else {
            items(module.chapters, key = { "chapter-${it.id}" }) { chapter ->
                ChapterCard(chapter = chapter)
            }
        }

        item {
            SectionHeader(title = "Study Tools")
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToSummary,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Summary")
                }
                Button(
                    onClick = onNavigateToFlashcards,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cards")
                }
                Button(
                    onClick = onNavigateToQuiz,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Quiz")
                }
            }
        }
    }
}

@Composable
private fun ModuleHeaderCard(
    module: LearningModule,
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
                StatusChip(status = module.contentType.ifBlank { "Module" })
                Text(
                    text = "Updated ${module.updatedAt}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = module.title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = module.subjectTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (module.description.isNotBlank()) {
                Text(
                    text = module.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            ProgressBar(progress = module.progressPercentage.coerceIn(0, 100) / 100f)
            if (isRefreshing) {
                Text(
                    text = "Refreshing module...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ReaderTextCard(
    title: String,
    text: String
) {
    StudyLensCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
    }
}

@Composable
private fun ChapterCard(chapter: LearningChapter) {
    val text = chapter.readerText()

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
                    text = chapter.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = "Ch. ${chapter.order}")
            }
            if (text.isBlank()) {
                Text(
                    text = "No readable text yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(top = 10.dp)
                )
            } else {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

private fun LearningModule.readerText(): String {
    return markdownContent.ifBlank { extractedText }
}

private fun LearningChapter.readerText(): String {
    return markdownContent.ifBlank { extractedText }
}
