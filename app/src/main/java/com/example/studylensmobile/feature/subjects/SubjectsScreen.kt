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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.domain.model.Subject
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

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = "Subjects",
                actions = {
                    IconButton(onClick = viewModel::loadSubjects) {
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
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
@Composable
private fun SubjectsContent(
    uiState: SubjectsUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onNavigateToSubjectDetail: (String) -> Unit,
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
            items(uiState.subjects, key = { it.id }) { subject ->
                SubjectCard(
                    subject = subject,
                    onClick = { onNavigateToSubjectDetail(subject.id) }
                )
            }
        }
    }
}
@Composable
private fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit
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
                StatusChip(status = subject.code)
                Text(
                    text = "${subject.progressPercentage}%",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
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
