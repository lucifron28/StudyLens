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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.domain.model.SubjectBoardScanPreview
import com.example.studylensmobile.domain.model.SubjectModulePreview
import com.example.studylensmobile.domain.model.SubjectOverview
import com.example.studylensmobile.domain.model.StudyTaskPreview
import com.example.studylensmobile.core.utils.displayName
import com.example.studylensmobile.ui.components.DeleteConfirmationDialog
import com.example.studylensmobile.ui.components.ModuleFormDialog
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensEmptyState
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensInlineError
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Checkbox
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.FilterChip
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensTopBar
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
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var showAddActionSheet by remember { mutableStateOf(false) }
    var editingModule by remember { mutableStateOf<SubjectModulePreview?>(null) }
    var deletingModule by remember { mutableStateOf<SubjectModulePreview?>(null) }
    var editingTask by remember { mutableStateOf<StudyTaskPreview?>(null) }
    var deletingTask by remember { mutableStateOf<StudyTaskPreview?>(null) }

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
                        onClick = { showAddActionSheet = true },
                        enabled = !uiState.isMutating
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to subject"
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
                    onEditTask = { editingTask = it },
                    onDeleteTask = { deletingTask = it },
                    onTaskChecked = { task, isChecked ->
                        viewModel.updateTask(
                            taskId = task.id.toString(),
                            title = task.title,
                            content = task.content,
                            taskType = task.taskType,
                            isCompleted = isChecked,
                            dueDate = task.dueDate,
                            isPinned = task.isPinned
                        )
                    },
                    actionsEnabled = !uiState.isMutating,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    if (showAddActionSheet) {
        ModalBottomSheet(onDismissRequest = { showAddActionSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add to Subject",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ListItem(
                    headlineContent = { Text("Add Module") },
                    supportingContent = { Text("Create a new learning module") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        showAddActionSheet = false
                        showCreateModuleDialog = true 
                    }
                )
                ListItem(
                    headlineContent = { Text("Add Task / Note") },
                    supportingContent = { Text("Create a to-do item, reminder, or note") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        showAddActionSheet = false
                        showCreateTaskDialog = true 
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showCreateModuleDialog && overview != null) {
        ModuleFormDialog(
            titleText = "Add module",
            initialTitle = "",
            initialDescription = "",
            initialContentType = "markdown",
            initialMarkdownContent = "",
            isSaving = uiState.isMutating,
            onDismiss = { showCreateModuleDialog = false },
            onSave = { title, description, contentType, markdownContent, fileUri ->
                viewModel.createModule(
                    title = title,
                    description = description,
                    contentType = contentType,
                    markdownContent = markdownContent,
                    fileUri = fileUri,
                    onSaved = { showCreateModuleDialog = false }
                )
            }
        )
    }

    editingModule?.let { module ->
        ModuleFormDialog(
            titleText = "Edit module",
            initialTitle = module.title,
            initialDescription = module.description,
            initialContentType = module.contentType,
            initialMarkdownContent = "",
            isSaving = uiState.isMutating,
            onDismiss = { editingModule = null },
            onSave = { title, description, contentType, markdownContent, fileUri ->
                viewModel.updateModule(
                    moduleId = module.id,
                    title = title,
                    description = description,
                    contentType = contentType,
                    markdownContent = markdownContent,
                    fileUri = fileUri,
                    onSaved = { editingModule = null }
                )
            }
        )
    }

    deletingModule?.let { module ->
        DeleteConfirmationDialog(
            title = "Delete module?",
            message = "This will delete ${module.title} and its related notes and study tools.",
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

    if (showCreateTaskDialog && overview != null) {
        StudyTaskFormDialog(
            task = null,
            isSaving = uiState.isMutating,
            onDismiss = { showCreateTaskDialog = false },
            onSave = { title, content, taskType, isCompleted, dueDate, isPinned ->
                viewModel.createTask(
                    title = title,
                    content = content,
                    taskType = taskType,
                    isCompleted = isCompleted,
                    dueDate = dueDate,
                    isPinned = isPinned,
                    onSaved = { showCreateTaskDialog = false }
                )
            }
        )
    }

    editingTask?.let { task ->
        StudyTaskFormDialog(
            task = task,
            isSaving = uiState.isMutating,
            onDismiss = { editingTask = null },
            onSave = { title, content, taskType, isCompleted, dueDate, isPinned ->
                viewModel.updateTask(
                    taskId = task.id.toString(),
                    title = title,
                    content = content,
                    taskType = taskType,
                    isCompleted = isCompleted,
                    dueDate = dueDate,
                    isPinned = isPinned,
                    onSaved = { editingTask = null }
                )
            }
        )
    }

    deletingTask?.let { task ->
        DeleteConfirmationDialog(
            title = "Delete task/log?",
            message = "Are you sure you want to delete ${task.title}?",
            isDeleting = uiState.isMutating,
            onConfirm = {
                viewModel.deleteTask(
                    taskId = task.id.toString(),
                    onDeleted = { deletingTask = null }
                )
            },
            onDismiss = { deletingTask = null }
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
    onEditTask: (StudyTaskPreview) -> Unit,
    onDeleteTask: (StudyTaskPreview) -> Unit,
    onTaskChecked: (StudyTaskPreview, Boolean) -> Unit,
    actionsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedTaskFilter by remember { mutableStateOf(TaskNoteFilter.ALL) }
    val filteredTasks = remember(overview.tasks, selectedTaskFilter) {
        overview.tasks.filter { task -> selectedTaskFilter.matches(task) }
    }

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
            SectionHeader(title = "Tasks & Notes")
        }
        if (overview.tasks.isNotEmpty()) {
            item {
                TaskNoteFilterRow(
                    selectedFilter = selectedTaskFilter,
                    onFilterSelected = { selectedTaskFilter = it }
                )
            }
        }
        if (overview.tasks.isEmpty()) {
            item {
                StudyLensEmptyState(text = "No tasks or notes recorded yet.")
            }
        } else if (filteredTasks.isEmpty()) {
            item {
                StudyLensEmptyState(text = "No ${selectedTaskFilter.emptyLabel} recorded yet.")
            }
        } else {
            items(filteredTasks, key = { "task-${it.id}" }) { task ->
                StudyTaskPreviewCard(
                    task = task,
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) },
                    onCheckedChange = { isChecked -> onTaskChecked(task, isChecked) },
                    actionsEnabled = actionsEnabled
                )
            }
        }
    }
}

@Composable
private fun TaskNoteFilterRow(
    selectedFilter: TaskNoteFilter,
    onFilterSelected: (TaskNoteFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskNoteFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) }
            )
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
            StatusChip(status = subject.code)
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
private fun StudyTaskPreviewCard(
    task: StudyTaskPreview,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    actionsEnabled: Boolean
) {
    val isActionable = task.taskType.lowercase() in actionableTaskTypes
    val itemLabel = if (isActionable) "task" else "note"

    StudyLensCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isActionable) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onCheckedChange,
                    enabled = actionsEnabled,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(status = task.taskType.toDisplayLabel())
                }
                if (task.content.isNotBlank()) {
                    Text(
                        text = task.content,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isActionable) task.createdAt else "Saved ${task.createdAt}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (task.dueDate != null) {
                        Text(
                            text = "Due: ${task.dueDate}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Row {
                    IconButton(
                        onClick = onEdit,
                        enabled = actionsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit $itemLabel",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        enabled = actionsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete $itemLabel",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

private val actionableTaskTypes = setOf("todo", "reminder")

private enum class TaskNoteFilter(
    val label: String,
    val emptyLabel: String
) {
    ALL("All", "items"),
    TASKS("Tasks", "tasks"),
    NOTES("Notes", "notes");

    fun matches(task: StudyTaskPreview): Boolean {
        return when (this) {
            ALL -> true
            TASKS -> task.taskType.lowercase() in actionableTaskTypes
            NOTES -> task.taskType.lowercase() !in actionableTaskTypes
        }
    }
}
