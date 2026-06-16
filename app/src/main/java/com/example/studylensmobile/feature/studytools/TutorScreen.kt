package com.example.studylensmobile.feature.studytools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.domain.model.TutorMessage
import com.example.studylensmobile.domain.model.TutorSession
import com.example.studylensmobile.ui.components.LumiCard
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.StatusChip

@Composable
fun TutorScreen(
    viewModel: TutorViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = "AI Tutor",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::startTutor) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart tutor"
                        )
                    }
                }
            )
        },
        bottomBar = {
            TutorInputBar(
                value = uiState.draftMessage,
                enabled = !uiState.isStarting && !uiState.isSending && uiState.session?.isMastered != true,
                canSend = uiState.canSend,
                onValueChange = viewModel::updateDraft,
                onSend = viewModel::sendMessage,
                onNeedHint = viewModel::askForHint
            )
        }
    ) { padding ->
        when {
            uiState.isStarting && uiState.messages.isEmpty() -> {
                StudyLensLoadingState(
                    message = "Starting tutor...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            uiState.session == null -> {
                StudyLensErrorState(
                    message = uiState.errorMessage ?: "Tutor session is unavailable.",
                    onRetry = viewModel::startTutor,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            else -> {
                TutorContent(
                    uiState = uiState,
                    onDone = onBack,
                    onRestart = viewModel::startTutor,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun TutorContent(
    uiState: TutorUiState,
    onDone: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TutorHeader(session = uiState.session, progressLabel = uiState.progressLabel)
        }

        if (uiState.errorMessage != null) {
            item {
                StudyLensCard {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        items(uiState.messages, key = { it.id }) { message ->
            TutorMessageBubble(message = message)
        }

        if (uiState.isSending) {
            item {
                Text(
                    text = "Tutor is checking your answer...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (uiState.session?.isMastered == true) {
            item {
                LumiCard(
                    title = "Congrats, topic mastered",
                    message = "You answered ${uiState.session.clearAnswersCount} clear responses. This tutor round is complete.",
                    primaryActionLabel = "Done",
                    onPrimaryAction = onDone,
                    secondaryActionLabel = "Try Again",
                    onSecondaryAction = onRestart
                )
            }
        }
    }
}

@Composable
private fun TutorHeader(
    session: TutorSession?,
    progressLabel: String
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
                    text = session?.title ?: "AI Tutor",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = session?.status ?: "Starting")
            }
            Text(
                text = progressLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TutorMessageBubble(message: TutorMessage) {
    val bubbleColor = if (message.isFromStudent) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (message.isFromStudent) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromStudent) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(0.86f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.content,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (message.clarityResult.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip(status = message.clarityResult)
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorInputBar(
    value: String,
    enabled: Boolean,
    canSend: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onNeedHint: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                minLines = 1,
                maxLines = 4,
                placeholder = { Text("Type your answer") }
            )
            Button(
                onClick = onSend,
                enabled = canSend
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send answer"
                )
            }
        }
        OutlinedButton(
            onClick = onNeedHint,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I don't know yet")
        }
    }
}
