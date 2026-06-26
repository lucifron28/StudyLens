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
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.R
import com.example.studylensmobile.domain.model.LearningModule
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Edit
import com.example.studylensmobile.ui.components.ModuleFormDialog
import com.example.studylensmobile.ui.components.MarkdownText
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.SectionHeader
import com.example.studylensmobile.ui.components.StatusChip

@Composable
fun ModuleReaderScreen(
    viewModel: ModuleReaderViewModel,
    onBack: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTutor: () -> Unit,
    onNavigateToPdfViewer: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val module = uiState.module
    var showEditDialog by remember { mutableStateOf(false) }

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
                    if (module != null) {
                        IconButton(onClick = { showEditDialog = true }, enabled = !uiState.isMutating) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit module"
                            )
                        }
                    }
                    IconButton(onClick = viewModel::loadModule, enabled = !uiState.isMutating) {
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
                    onNavigateToTutor = onNavigateToTutor,
                    onNavigateToPdfViewer = onNavigateToPdfViewer,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (showEditDialog && module != null) {
        ModuleFormDialog(
            titleText = "Edit module",
            initialTitle = module.title,
            initialDescription = module.description,
            initialContentType = module.contentType,
            initialMarkdownContent = module.markdownContent ?: "",
            isSaving = uiState.isMutating,
            onDismiss = { showEditDialog = false },
            onSave = { title, description, contentType, markdownContent, _ ->
                viewModel.updateModule(
                    title = title,
                    description = description,
                    contentType = contentType,
                    markdownContent = markdownContent,
                    onSaved = { showEditDialog = false }
                )
            }
        )
    }
}

@Composable
private fun ModuleReaderContent(
    module: LearningModule,
    isRefreshing: Boolean,
    onNavigateToSummary: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTutor: () -> Unit,
    onNavigateToPdfViewer: (String) -> Unit,
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
        
        if (module.canOpenInPdfViewer()) {
            item {
                Button(
                    onClick = { onNavigateToPdfViewer(module.moduleFileUrl.orEmpty()) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Open PDF Viewer")
                }
            }
        } else if (module.moduleFileUrl != null) {
            item {
                Text(
                    text = "Document attached. Its extracted text is shown below.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            ReaderTextCard(
                title = module.title,
                text = module.readerText().ifBlank { "No readable content yet." }
            )
        }

        item {
            SectionHeader(title = "Study Tools")
        }

        item {
            StudyToolsBanner(
                onNavigateToSummary = onNavigateToSummary,
                onNavigateToFlashcards = onNavigateToFlashcards,
                onNavigateToQuiz = onNavigateToQuiz,
                onNavigateToTutor = onNavigateToTutor
            )
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
            MarkdownText(
                markdown = text,
                color = MaterialTheme.colorScheme.onSurface,
                bodyStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
    }
}

private fun LearningModule.readerText(): String {
    return markdownContent.ifBlank { extractedText }
}

private fun LearningModule.canOpenInPdfViewer(): Boolean {
    return moduleFileUrl != null && contentType.equals("pdf", ignoreCase = true)
}

@Composable
private fun StudyToolsBanner(
    onNavigateToSummary: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTutor: () -> Unit
) {
    StudyLensCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.lumi_thinking),
                contentDescription = "Lumi Mascot",
                modifier = Modifier.size(96.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Ready to test yourself?",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionChip(
                            text = "Summary",
                            icon = Icons.Default.Summarize,
                            onClick = onNavigateToSummary,
                            modifier = Modifier.weight(1f)
                        )
                        ActionChip(
                            text = "Cards",
                            icon = Icons.Default.Style,
                            onClick = onNavigateToFlashcards,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionChip(
                            text = "Quiz",
                            icon = Icons.Default.Quiz,
                            onClick = onNavigateToQuiz,
                            modifier = Modifier.weight(1f)
                        )
                        ActionChip(
                            text = "Tutor",
                            icon = Icons.Default.SmartToy,
                            onClick = onNavigateToTutor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionChip(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}
