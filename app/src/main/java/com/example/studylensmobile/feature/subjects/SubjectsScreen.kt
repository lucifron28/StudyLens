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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.domain.model.Subject
import com.example.studylensmobile.ui.components.DeleteConfirmationDialog
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensEmptyState
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensInlineError
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensRefreshingIndicator
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.ProgressBar
import com.example.studylensmobile.ui.components.StatusChip

@Composable
fun SubjectsScreen(
    viewModel: SubjectsViewModel,
    onNavigateToSubjectDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }
    var deletingSubject by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = "Subjects",
                actions = {
                    IconButton(
                        onClick = { showCreateDialog = true },
                        enabled = !uiState.isMutating
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add subject"
                        )
                    }
                    IconButton(
                        onClick = viewModel::loadSubjects,
                        enabled = !uiState.isMutating
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh subjects"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                StudyLensLoadingState(
                    message = "Loading subjects...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            uiState.subjects.isEmpty() && uiState.errorMessage != null -> {
                StudyLensErrorState(
                    message = uiState.errorMessage ?: "Subjects are unavailable.",
                    onRetry = viewModel::loadSubjects,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            else -> {
                SubjectsContent(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onSearch = viewModel::loadSubjects,
                    onRetry = viewModel::loadSubjects,
                    onNavigateToSubjectDetail = onNavigateToSubjectDetail,
                    onEditSubject = { editingSubject = it },
                    onDeleteSubject = { deletingSubject = it },
                    actionsEnabled = !uiState.isMutating,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (showCreateDialog) {
        SubjectFormDialog(
            subject = null,
            isSaving = uiState.isMutating,
            onDismiss = { showCreateDialog = false },
            onSave = { title, description ->
                viewModel.createSubject(
                    title = title,
                    description = description,
                    onSaved = { showCreateDialog = false }
                )
            }
        )
    }

    editingSubject?.let { subject ->
        SubjectFormDialog(
            subject = subject,
            isSaving = uiState.isMutating,
            onDismiss = { editingSubject = null },
            onSave = { title, description ->
                viewModel.updateSubject(
                    subjectId = subject.id,
                    title = title,
                    description = description,
                    onSaved = { editingSubject = null }
                )
            }
        )
    }

    deletingSubject?.let { subject ->
        DeleteConfirmationDialog(
            title = "Delete subject?",
            message = "This will delete ${subject.title} and its modules, notes, posts, and progress.",
            isDeleting = uiState.isMutating,
            onConfirm = {
                viewModel.deleteSubject(
                    subjectId = subject.id,
                    onDeleted = { deletingSubject = null }
                )
            },
            onDismiss = { deletingSubject = null }
        )
    }
}
@Composable
private fun SubjectsContent(
    uiState: SubjectsUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onNavigateToSubjectDetail: (String) -> Unit,
    onEditSubject: (Subject) -> Unit,
    onDeleteSubject: (Subject) -> Unit,
    actionsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search subjects...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() })
            )
        }

        if (uiState.errorMessage != null && uiState.subjects.isNotEmpty()) {
            item {
                StudyLensInlineError(message = uiState.errorMessage, onRetry = onRetry)
            }
        }

        if (uiState.isRefreshing) {
            item {
                StudyLensRefreshingIndicator()
            }
        }

        if (uiState.subjects.isEmpty()) {
            item {
                StudyLensEmptyState(text = "No subjects found yet.")
            }
        } else {
            items(uiState.subjects, key = { "subject-${it.id}" }) { subject ->
                SubjectCard(
                    subject = subject,
                    onClick = { onNavigateToSubjectDetail(subject.id) },
                    onEdit = { onEditSubject(subject) },
                    onDelete = { onDeleteSubject(subject) },
                    actionsEnabled = actionsEnabled
                )
            }
        }
    }
}
@Composable
private fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    actionsEnabled: Boolean
) {
    StudyLensCard(onClick = onClick) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(status = subject.code)
                    Text(
                        text = "${subject.progressPercentage}%",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Row {
                    IconButton(
                        onClick = onEdit,
                        enabled = actionsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit subject"
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        enabled = actionsEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete subject"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = subject.title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subject.description.isNotBlank()) {
                Text(
                    text = subject.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                text = subject.itemSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp)
            )
            ProgressBar(
                progress = subject.progressPercentage.coerceIn(0, 100) / 100f,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun SubjectFormDialog(
    subject: Subject?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String) -> Unit
) {
    var title by remember(subject?.id) { mutableStateOf(subject?.title.orEmpty()) }
    var description by remember(subject?.id) { mutableStateOf(subject?.description.orEmpty()) }
    var validationMessage by remember(subject?.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        title = { Text(if (subject == null) "Add subject" else "Edit subject") },
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
                    minLines = 3,
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
                    if (cleanedTitle.isBlank()) {
                        validationMessage = "Subject title is required."
                    } else {
                        onSave(cleanedTitle, description.trim())
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
