package com.example.studylensmobile.feature.subjects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.domain.model.StudyTaskPreview
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTaskFormDialog(
    task: StudyTaskPreview? = null,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, taskType: String, isCompleted: Boolean, dueDate: String?, isPinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var content by remember { mutableStateOf(task?.content ?: "") }
    var taskType by remember { mutableStateOf(task?.taskType ?: "todo") }
    var isCompleted by remember { mutableStateOf(task?.isCompleted ?: false) }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: "") }
    var isPinned by remember { mutableStateOf(task?.isPinned ?: false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    
    val validTypes = listOf("todo", "note", "reminder", "update")

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = { Text(if (task == null) "Add task/log" else "Edit task/log") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
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
                }
                item {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        enabled = !isSaving,
                        label = { Text("Content") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = taskType,
                        onValueChange = {
                            taskType = it
                            validationMessage = null
                        },
                        enabled = !isSaving,
                        label = { Text("Task type") },
                        placeholder = { Text("todo, note, reminder, update") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        enabled = !isSaving,
                        label = { Text("Due Date (Optional, YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(
                                checked = isPinned,
                                onCheckedChange = { isPinned = it },
                                enabled = !isSaving
                            )
                            Text("Pinned")
                        }
                        
                        if (taskType.lowercase(Locale.getDefault()) in listOf("todo", "reminder")) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isCompleted,
                                    onCheckedChange = { isCompleted = it },
                                    enabled = !isSaving
                                )
                                Text("Completed")
                            }
                        }
                    }
                }
                item {
                    validationMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanedTitle = title.trim()
                    val cleanedType = taskType.trim().lowercase(Locale.getDefault())
                    when {
                        cleanedTitle.isBlank() -> validationMessage = "Title is required."
                        cleanedType !in validTypes -> validationMessage = "Use todo, note, reminder, or update."
                        else -> {
                            onSave(
                                cleanedTitle,
                                content.trim(),
                                cleanedType,
                                isCompleted,
                                dueDate.trim().takeIf { it.isNotBlank() },
                                isPinned
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
