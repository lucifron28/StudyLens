package com.example.studylensmobile.feature.subjects

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.domain.model.SubjectBoardScanPreview
import com.example.studylensmobile.domain.model.SubjectModulePreview
import com.example.studylensmobile.domain.model.SubjectOverview
import com.example.studylensmobile.domain.model.SubjectPostPreview
import com.example.studylensmobile.domain.model.SubjectTaskPreview
import com.example.studylensmobile.ui.components.DeleteConfirmationDialog
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensEmptyState
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensInlineError
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.ProgressBar
import com.example.studylensmobile.ui.components.SectionHeader
import com.example.studylensmobile.ui.components.StatusChip

@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel,
    onBack: () -> Unit,
    onNavigateToModuleReader: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val overview = uiState.overview
    var showCreateModuleDialog by remember { mutableStateOf(false) }
    var editingModule by remember { mutableStateOf<SubjectModulePreview?>(null) }
    var deletingModule by remember { mutableStateOf<SubjectModulePreview?>(null) }

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = overview?.subject?.title ?: "Subject Detail",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCreateModuleDialog = true },
                        enabled = !uiState.isMutating
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add module"
                        )
                    }
                    IconButton(
                        onClick = viewModel::loadOverview,
                        enabled = !uiState.isMutating
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh subject"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                StudyLensLoadingState(
                    message = "Loading subject...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            overview == null -> {
                StudyLensErrorState(
                    message = uiState.errorMessage ?: "Subject details are unavailable.",
                    onRetry = viewModel::loadOverview,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            else -> {
                SubjectOverviewContent(
                    overview = overview,
                    modules = uiState.modules,
                    errorMessage = uiState.errorMessage,
                    isRefreshing = uiState.isRefreshing,
                    onRetry = viewModel::loadOverview,
                    onNavigateToModuleReader = onNavigateToModuleReader,
                    onEditModule = { editingModule = it },
                    onDeleteModule = { deletingModule = it },
                    actionsEnabled = !uiState.isMutating,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (showCreateModuleDialog && overview != null) {
        ModuleFormDialog(
            module = null,
            isSaving = uiState.isMutating,
            onDismiss = { showCreateModuleDialog = false },
            onSave = { title, description, contentType, markdownContent ->
                viewModel.createModule(
                    title = title,
                    description = description,
                    contentType = contentType,
                    markdownContent = markdownContent,
                    onSaved = { showCreateModuleDialog = false }
                )
            }
        )
    }

    editingModule?.let { module ->
        ModuleFormDialog(
            module = module,
            isSaving = uiState.isMutating,
            onDismiss = { editingModule = null },
            onSave = { title, description, contentType, markdownContent ->
                viewModel.updateModule(
                    moduleId = module.id,
                    title = title,
                    description = description,
                    contentType = contentType,
                    markdownContent = markdownContent,
                    onSaved = { editingModule = null }
                )
            }
        )
    }

    deletingModule?.let { module ->
        DeleteConfirmationDialog(
            title = "Delete module?",
            message = "This will delete ${module.title} and its chapters, notes, progress, and study tools.",
            isDeleting = uiState.isMutating,
            onConfirm = {
                viewModel.deleteModule(
                    moduleId = module.id,
                    onDeleted = { deletingModule = null }
                )
            },
            onDismiss = { deletingModule = null }
        )
    }
}
@Composable
private fun SubjectOverviewContent(
    overview: SubjectOverview,
    modules: List<SubjectModulePreview>,
    errorMessage: String?,
    isRefreshing: Boolean,
    onRetry: () -> Unit,
    onNavigateToModuleReader: (String) -> Unit,
    onEditModule: (SubjectModulePreview) -> Unit,
    onDeleteModule: (SubjectModulePreview) -> Unit,
    actionsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeaderCard(overview = overview, isRefreshing = isRefreshing)
        }

        if (errorMessage != null && !isRefreshing) {
            item {
                StudyLensInlineError(message = errorMessage, onRetry = onRetry)
            }
        }

        item {
            SectionHeader(title = "Modules")
        }
        if (modules.isEmpty()) {
            item {
                StudyLensEmptyState(text = "No modules added yet.")
            }
        } else {
            items(modules, key = { "module-${it.id}" }) { module ->
                ModulePreviewCard(
                    module = module,
                    onClick = { onNavigateToModuleReader(module.id) },
                    onEdit = { onEditModule(module) },
                    onDelete = { onDeleteModule(module) },
                    actionsEnabled = actionsEnabled
                )
            }
        }

        item {
            SectionHeader(title = "Upcoming Tasks")
        }
        if (overview.upcomingTasks.isEmpty()) {
            item {
                StudyLensEmptyState(text = "No upcoming tasks for this subject.")
            }
        } else {
            items(overview.upcomingTasks, key = { "task-${it.id}" }) { task ->
                TaskPreviewCard(task = task)
            }
        }

        item {
            SectionHeader(title = "Recent Board Notes")
        }
        if (overview.recentBoardScans.isEmpty()) {
            item {
                StudyLensEmptyState(text = "No board notes saved for this subject.")
            }
        } else {
            items(overview.recentBoardScans, key = { "board-scan-${it.id}" }) { boardScan ->
                BoardScanPreviewCard(boardScan = boardScan)
            }
        }

        item {
            SectionHeader(title = "Latest Posts")
        }
        if (overview.latestPosts.isEmpty()) {
            item {
                StudyLensEmptyState(text = "No posts for this subject yet.")
            }
        } else {
            items(overview.latestPosts, key = { "post-${it.id}" }) { post ->
                PostPreviewCard(post = post)
            }
        }
    }
}
@Composable
private fun HeaderCard(
    overview: SubjectOverview,
    isRefreshing: Boolean
) {
    val subject = overview.subject
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
                StatusChip(status = subject.code)
                Text(
                    text = "${subject.progressPercentage}%",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = subject.title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (subject.description.isNotBlank()) {
                Text(
                    text = subject.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Text(
                text = subject.itemSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
            ProgressBar(
                progress = subject.progressPercentage.coerceIn(0, 100) / 100f,
                modifier = Modifier.padding(top = 12.dp)
            )
            if (isRefreshing) {
                Text(
                    text = "Refreshing subject details...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ModulePreviewCard(
    module: SubjectModulePreview,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    actionsEnabled: Boolean
) {
    StudyLensCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = module.description.ifBlank { "Updated ${module.updatedAt}" },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(status = module.contentType)
                Row {
                    IconButton(
                        onClick = onEdit,
                        enabled = actionsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit module"
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        enabled = actionsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete module"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleFormDialog(
    module: SubjectModulePreview?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        contentType: String,
        markdownContent: String
    ) -> Unit
) {
    var title by remember(module?.id) { mutableStateOf(module?.title.orEmpty()) }
    var description by remember(module?.id) { mutableStateOf(module?.description.orEmpty()) }
    var contentType by remember(module?.id) { mutableStateOf(module?.contentType?.lowercase() ?: "markdown") }
    var markdownContent by remember(module?.id) { mutableStateOf("") }
    var validationMessage by remember(module?.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        title = { Text(if (module == null) "Add module" else "Edit module") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        validationMessage = null
                    },
                    enabled = !isSaving,
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    enabled = !isSaving,
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contentType,
                    onValueChange = {
                        contentType = it
                        validationMessage = null
                    },
                    enabled = !isSaving,
                    label = { Text("Content type") },
                    placeholder = { Text("markdown, text, pdf, docx, pptx") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = markdownContent,
                    onValueChange = { markdownContent = it },
                    enabled = !isSaving,
                    label = { Text(if (module == null) "Module content" else "New content optional") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                validationMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanedTitle = title.trim()
                    val cleanedContentType = contentType.trim().lowercase()
                    when {
                        cleanedTitle.isBlank() -> validationMessage = "Module title is required."
                        cleanedContentType !in validModuleContentTypes -> {
                            validationMessage = "Use markdown, text, pdf, docx, or pptx."
                        }
                        else -> {
                            onSave(
                                cleanedTitle,
                                description.trim(),
                                cleanedContentType,
                                markdownContent.trim()
                            )
                        }
                    }
                },
                enabled = !isSaving && title.trim().isNotBlank()
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
        }
    )
}

private val validModuleContentTypes = setOf("markdown", "text", "pdf", "docx", "pptx")

@Composable
private fun TaskPreviewCard(task: SubjectTaskPreview) {
    StudyLensCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOf(task.taskType, task.priority, task.dueAt.orEmpty())
                        .filter { it.isNotBlank() }
                        .joinToString(" - "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusChip(status = task.status)
        }
    }
}

@Composable
private fun BoardScanPreviewCard(boardScan: SubjectBoardScanPreview) {
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
                    text = boardScan.createdAt,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                StatusChip(status = boardScan.reviewStatus)
            }
            Text(
                text = boardScan.cleanedText.ifBlank { "No cleaned OCR text yet." },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun PostPreviewCard(post: SubjectPostPreview) {
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
                    text = post.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = post.postType)
            }
            Text(
                text = post.content,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = post.postedAt,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
