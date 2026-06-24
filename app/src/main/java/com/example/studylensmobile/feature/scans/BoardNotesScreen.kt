package com.example.studylensmobile.feature.scans

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.domain.model.BoardScan
import com.example.studylensmobile.domain.model.LearningChapter
import com.example.studylensmobile.domain.model.Subject
import com.example.studylensmobile.domain.model.SubjectModulePreview
import com.example.studylensmobile.ui.components.DeleteConfirmationDialog
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensEmptyState
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensInlineError
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensRefreshingIndicator
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.StatusChip

@Composable
fun BoardNotesScreen(
    viewModel: BoardNotesViewModel,
    onNavigateToOcrResult: (String) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    onNavigateToFlashcards: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToTutor: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.recognizeBoardImage(context, it) }
    }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingScan by remember { mutableStateOf<BoardScan?>(null) }
    var deletingScan by remember { mutableStateOf<BoardScan?>(null) }
    val openImagePicker = {
        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    val cameraEnabled = !uiState.isMutating && !uiState.isRecognizingText

    LaunchedEffect(uiState.isRecognizingText, uiState.ocrDraftText, editingScan) {
        if (editingScan == null && (uiState.isRecognizingText || uiState.ocrDraftText.isNotBlank())) {
            showCreateDialog = true
        }
    }


    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = "Board Notes",
                actions = {
                    IconButton(
                        onClick = { showCreateDialog = true },
                        enabled = !uiState.isMutating
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add board note"
                        )
                    }
                    IconButton(
                        onClick = viewModel::loadBoardScans,
                        enabled = !uiState.isMutating
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh board notes"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (cameraEnabled) {
                        onNavigateToCamera()
                    }
                },
                containerColor = if (cameraEnabled) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (cameraEnabled) {
                    MaterialTheme.colorScheme.onSecondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Capture board image"
                )
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                StudyLensLoadingState(
                    message = "Loading board notes...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            uiState.boardScans.isEmpty() && uiState.errorMessage != null -> {
                StudyLensErrorState(
                    message = uiState.errorMessage ?: "Board notes are unavailable.",
                    onRetry = viewModel::loadBoardScans,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            else -> {
                BoardNotesContent(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onSearch = viewModel::loadBoardScans,
                    onRetry = viewModel::loadBoardScans,
                    onNavigateToOcrResult = onNavigateToOcrResult,
                    onNavigateToSummary = onNavigateToSummary,
                    onNavigateToFlashcards = onNavigateToFlashcards,
                    onNavigateToQuiz = onNavigateToQuiz,
                    onNavigateToTutor = onNavigateToTutor,
                    onEditScan = { editingScan = it },
                    onDeleteScan = { deletingScan = it },
                    actionsEnabled = !uiState.isMutating,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (showCreateDialog) {
        BoardNoteFormDialog(
            scan = null,
            isSaving = uiState.isMutating,
            isRecognizingText = uiState.isRecognizingText,
            ocrDraftText = uiState.ocrDraftText,
            pendingImageUri = uiState.pendingImageUri,
            availableSubjects = uiState.availableSubjects,
            availableModules = uiState.availableModules,
            availableChapters = uiState.availableChapters,
            isLoadingModules = uiState.isLoadingModules,
            isLoadingChapters = uiState.isLoadingChapters,
            onPickImage = openImagePicker,
            onOcrDraftApplied = viewModel::clearOcrDraft,
            onSubjectSelected = { subjectId ->
                if (subjectId != null) viewModel.fetchModulesForSubject(subjectId)
                else viewModel.clearModulesAndChapters()
            },
            onModuleSelected = { moduleId ->
                if (moduleId != null) viewModel.fetchChaptersForModule(moduleId)
                else viewModel.clearModulesAndChapters()
            },
            onDismiss = {
                viewModel.discardPendingImage()
                showCreateDialog = false
            },
            onSave = { rawText, cleanedText, summary, reviewStatus, subjectId, moduleId, chapterId, imageUri ->
                viewModel.createBoardScan(
                    rawOcrText = rawText,
                    cleanedText = cleanedText,
                    summary = summary,
                    reviewStatus = reviewStatus,
                    subjectId = subjectId,
                    moduleId = moduleId,
                    chapterId = chapterId,
                    imageUri = imageUri,
                    onSaved = { showCreateDialog = false }
                )
            }
        )
    }

    editingScan?.let { scan ->
        BoardNoteFormDialog(
            scan = scan,
            isSaving = uiState.isMutating,
            isRecognizingText = uiState.isRecognizingText,
            ocrDraftText = uiState.ocrDraftText,
            pendingImageUri = uiState.pendingImageUri,
            availableSubjects = uiState.availableSubjects,
            availableModules = uiState.availableModules,
            availableChapters = uiState.availableChapters,
            isLoadingModules = uiState.isLoadingModules,
            isLoadingChapters = uiState.isLoadingChapters,
            onPickImage = openImagePicker,
            onOcrDraftApplied = viewModel::clearOcrDraft,
            onSubjectSelected = { subjectId ->
                if (subjectId != null) viewModel.fetchModulesForSubject(subjectId)
                else viewModel.clearModulesAndChapters()
            },
            onModuleSelected = { moduleId ->
                if (moduleId != null) viewModel.fetchChaptersForModule(moduleId)
                else viewModel.clearModulesAndChapters()
            },
            onDismiss = {
                viewModel.discardPendingImage()
                editingScan = null
            },
            onSave = { rawText, cleanedText, summary, reviewStatus, subjectId, moduleId, chapterId, imageUri ->
                viewModel.updateBoardScan(
                    scanId = scan.id,
                    rawOcrText = rawText,
                    cleanedText = cleanedText,
                    summary = summary,
                    reviewStatus = reviewStatus,
                    subjectId = subjectId,
                    moduleId = moduleId,
                    chapterId = chapterId,
                    imageUri = imageUri,
                    onSaved = { editingScan = null }
                )
            }
        )
    }

    deletingScan?.let { scan ->
        DeleteConfirmationDialog(
            title = "Delete board note?",
            message = "This will permanently delete ${scan.title}.",
            isDeleting = uiState.isMutating,
            onConfirm = {
                viewModel.deleteBoardScan(
                    scanId = scan.id,
                    onDeleted = { deletingScan = null }
                )
            },
            onDismiss = { deletingScan = null }
        )
    }
}
@Composable
private fun BoardNotesContent(
    uiState: BoardNotesUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onNavigateToOcrResult: (String) -> Unit,
    onNavigateToSummary: (String) -> Unit,
    onNavigateToFlashcards: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToTutor: (String) -> Unit,
    onEditScan: (BoardScan) -> Unit,
    onDeleteScan: (BoardScan) -> Unit,
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
                placeholder = { Text("Search board notes...") },
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

        if (uiState.errorMessage != null && uiState.boardScans.isNotEmpty()) {
            item {
                StudyLensInlineError(message = uiState.errorMessage, onRetry = onRetry)
            }
        }

        if (uiState.isRefreshing) {
            item {
                StudyLensRefreshingIndicator()
            }
        }

        if (uiState.boardScans.isEmpty()) {
            item {
                StudyLensEmptyState(text = "No board notes saved yet.")
            }
        } else {
            items(uiState.boardScans, key = { "board-scan-${it.id}" }) { scan ->
                BoardScanCard(
                    scan = scan,
                    onClick = { onNavigateToOcrResult(scan.id) },
                    onNavigateToSummary = { onNavigateToSummary(scan.id) },
                    onNavigateToFlashcards = { onNavigateToFlashcards(scan.id) },
                    onNavigateToQuiz = { onNavigateToQuiz(scan.id) },
                    onNavigateToTutor = { onNavigateToTutor(scan.id) },
                    onEdit = { onEditScan(scan) },
                    onDelete = { onDeleteScan(scan) },
                    actionsEnabled = actionsEnabled
                )
            }
        }
    }
}
@Composable
private fun BoardScanCard(
    scan: BoardScan,
    onClick: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTutor: () -> Unit,
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
                Text(
                    text = scan.title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(status = scan.reviewStatus)
                    Row {
                        IconButton(
                            onClick = onEdit,
                            enabled = actionsEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit board note"
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            enabled = actionsEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete board note"
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = listOf(scan.subjectCode, scan.dateLabel)
                    .filter { it.isNotBlank() }
                    .joinToString(" - "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (scan.previewText.isNotBlank()) {
                Text(
                    text = scan.previewText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = onNavigateToSummary) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Generate Summary",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onNavigateToFlashcards) {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = "Generate Flashcards",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onNavigateToQuiz) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = "Generate Quiz",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onNavigateToTutor) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Start Tutor",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoardNoteFormDialog(
    scan: BoardScan?,
    isSaving: Boolean,
    isRecognizingText: Boolean,
    ocrDraftText: String,
    pendingImageUri: String?,
    availableSubjects: List<Subject>,
    availableModules: List<SubjectModulePreview>,
    availableChapters: List<LearningChapter>,
    isLoadingModules: Boolean,
    isLoadingChapters: Boolean,
    onPickImage: () -> Unit,
    onOcrDraftApplied: () -> Unit,
    onSubjectSelected: (String?) -> Unit,
    onModuleSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    onSave: (
        rawText: String,
        cleanedText: String,
        summary: String,
        reviewStatus: String,
        subjectId: String?,
        moduleId: String?,
        chapterId: String?,
        imageUri: String?
    ) -> Unit
) {
    var rawText by remember(scan?.id) { mutableStateOf(scan?.rawOcrText.orEmpty()) }
    var cleanedText by remember(scan?.id) {
        mutableStateOf(scan?.let { it.cleanedText.ifBlank { it.rawOcrText } }.orEmpty())
    }
    var summary by remember(scan?.id) { mutableStateOf(scan?.summary.orEmpty()) }
    var reviewStatus by remember(scan?.id) {
        mutableStateOf(scan?.reviewStatus?.lowercase()?.replace(" ", "_") ?: "new")
    }
    var subjectId by remember(scan?.id) { mutableStateOf(scan?.subjectId.orEmpty()) }
    var moduleId by remember(scan?.id) { mutableStateOf(scan?.moduleId.orEmpty()) }
    var chapterId by remember(scan?.id) { mutableStateOf(scan?.chapterId.orEmpty()) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var moduleDropdownExpanded by remember { mutableStateOf(false) }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }
    var validationMessage by remember(scan?.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(ocrDraftText) {
        if (ocrDraftText.isNotBlank()) {
            rawText = ocrDraftText
            cleanedText = ocrDraftText
            validationMessage = null
            onOcrDraftApplied()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        title = { Text(if (scan == null) "Add board note" else "Edit board note") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPickImage,
                    enabled = !isSaving && !isRecognizingText,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isRecognizingText) "Reading image..." else "Scan Image")
                }
                if (pendingImageUri != null) {
                    Text(
                        text = "Image attached and will be uploaded when you save.",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedTextField(
                    value = cleanedText,
                    onValueChange = {
                        cleanedText = it
                        validationMessage = null
                    },
                    enabled = !isSaving,
                    label = { Text("Cleaned text") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rawText,
                    onValueChange = {
                        rawText = it
                        validationMessage = null
                    },
                    enabled = !isSaving,
                    label = { Text("Raw OCR text") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    enabled = !isSaving,
                    label = { Text("Summary") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reviewStatus,
                    onValueChange = {
                        reviewStatus = it
                        validationMessage = null
                    },
                    enabled = !isSaving,
                    label = { Text("Review status") },
                    placeholder = { Text("new, needs_review, reviewed, mastered") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Subject dropdown
                val selectedSubjectLabel = availableSubjects
                    .firstOrNull { it.id == subjectId }?.title
                    ?: if (subjectId.isNotEmpty()) subjectId else ""
                ExposedDropdownMenuBox(
                    expanded = subjectDropdownExpanded,
                    onExpandedChange = { if (!isSaving) subjectDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedSubjectLabel,
                        onValueChange = {},
                        readOnly = true,
                        enabled = !isSaving,
                        label = { Text("Subject (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectDropdownExpanded,
                        onDismissRequest = { subjectDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                subjectId = ""
                                moduleId = ""
                                chapterId = ""
                                subjectDropdownExpanded = false
                                onSubjectSelected(null)
                            }
                        )
                        availableSubjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.title) },
                                onClick = {
                                    subjectId = subject.id
                                    moduleId = ""
                                    chapterId = ""
                                    subjectDropdownExpanded = false
                                    onSubjectSelected(subject.id)
                                }
                            )
                        }
                    }
                }

                // Module dropdown (enabled only if a subject is selected)
                val selectedModuleLabel = availableModules
                    .firstOrNull { it.id == moduleId }?.title
                    ?: if (moduleId.isNotEmpty()) moduleId else ""
                ExposedDropdownMenuBox(
                    expanded = moduleDropdownExpanded,
                    onExpandedChange = {
                        if (!isSaving && subjectId.isNotEmpty() && !isLoadingModules)
                            moduleDropdownExpanded = it
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (isLoadingModules) "Loading..." else selectedModuleLabel,
                        onValueChange = {},
                        readOnly = true,
                        enabled = !isSaving && subjectId.isNotEmpty() && !isLoadingModules,
                        label = { Text("Module (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = moduleDropdownExpanded) },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = moduleDropdownExpanded,
                        onDismissRequest = { moduleDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                moduleId = ""
                                chapterId = ""
                                moduleDropdownExpanded = false
                                onModuleSelected(null)
                            }
                        )
                        availableModules.forEach { module ->
                            DropdownMenuItem(
                                text = { Text(module.title) },
                                onClick = {
                                    moduleId = module.id
                                    chapterId = ""
                                    moduleDropdownExpanded = false
                                    onModuleSelected(module.id)
                                }
                            )
                        }
                    }
                }

                // Chapter dropdown (enabled only if a module is selected)
                val selectedChapterLabel = availableChapters
                    .firstOrNull { it.id == chapterId }?.title
                    ?: if (chapterId.isNotEmpty()) chapterId else ""
                ExposedDropdownMenuBox(
                    expanded = chapterDropdownExpanded,
                    onExpandedChange = {
                        if (!isSaving && moduleId.isNotEmpty() && !isLoadingChapters)
                            chapterDropdownExpanded = it
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (isLoadingChapters) "Loading..." else selectedChapterLabel,
                        onValueChange = {},
                        readOnly = true,
                        enabled = !isSaving && moduleId.isNotEmpty() && !isLoadingChapters,
                        label = { Text("Chapter (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = chapterDropdownExpanded,
                        onDismissRequest = { chapterDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                chapterId = ""
                                chapterDropdownExpanded = false
                            }
                        )
                        availableChapters.forEach { chapter ->
                            DropdownMenuItem(
                                text = { Text(chapter.title) },
                                onClick = {
                                    chapterId = chapter.id
                                    chapterDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
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
                    val cleanedRawText = rawText.trim()
                    val cleanedNoteText = cleanedText.trim()
                    val cleanedReviewStatus = reviewStatus.trim().lowercase()
                    when {
                        cleanedNoteText.ifBlank { cleanedRawText }.isBlank() -> {
                            validationMessage = "Board note text is required."
                        }
                        cleanedReviewStatus !in validReviewStatuses -> {
                            validationMessage = "Use new, needs_review, reviewed, or mastered."
                        }
                        else -> {
                            onSave(
                                cleanedRawText,
                                cleanedNoteText,
                                summary.trim(),
                                cleanedReviewStatus,
                                subjectId.trim().takeIf { it.isNotBlank() },
                                moduleId.trim().takeIf { it.isNotBlank() },
                                chapterId.trim().takeIf { it.isNotBlank() },
                                pendingImageUri
                            )
                        }
                    }
                },
                enabled = !isSaving && cleanedText.ifBlank { rawText }.trim().isNotBlank()
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

private val validReviewStatuses = setOf("new", "needs_review", "reviewed", "mastered")
