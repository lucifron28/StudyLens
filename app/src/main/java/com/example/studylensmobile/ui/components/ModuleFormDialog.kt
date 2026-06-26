package com.example.studylensmobile.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.core.utils.displayName

@Composable
fun ModuleFormDialog(
    titleText: String,
    initialTitle: String,
    initialDescription: String,
    initialContentType: String,
    initialMarkdownContent: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        contentType: String,
        markdownContent: String,
        fileUri: Uri?
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var contentType by remember { mutableStateOf(initialContentType.lowercase()) }
    var markdownContent by remember { mutableStateOf(initialMarkdownContent) }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        fileUri = uri
        if (uri != null) {
            val type = context.contentResolver.displayName(uri).lowercase()
            if (type.endsWith("pdf")) contentType = "pdf"
            else if (type.endsWith("docx")) contentType = "docx"
            else if (type.endsWith("pptx")) contentType = "pptx"
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        title = { Text(titleText) },
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
                        value = description,
                        onValueChange = { description = it },
                        enabled = !isSaving,
                        label = { Text("Description") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Button(
                        onClick = { filePickerLauncher.launch(supportedDocumentMimeTypes) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        enabled = !isSaving
                    ) {
                        val selectedFileName = fileUri?.let(context.contentResolver::displayName)
                        Text(selectedFileName?.let { "File Selected: $it" } ?: "Upload Document")
                    }
                }
                item {
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
                }
                if (contentType.trim().lowercase() in listOf("markdown", "text", "")) {
                    item {
                        OutlinedTextField(
                            value = markdownContent,
                            onValueChange = { markdownContent = it },
                            enabled = !isSaving,
                            label = { Text("Content (Optional)") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
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
                                markdownContent.trim(),
                                fileUri
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

private val supportedDocumentMimeTypes = arrayOf(
    "application/pdf",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
)
