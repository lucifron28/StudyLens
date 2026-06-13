package com.example.modulelensmobile.feature.scans

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
import com.example.modulelensmobile.domain.model.BoardScan
import com.example.modulelensmobile.ui.components.ModuleLensCard
import com.example.modulelensmobile.ui.components.ModuleLensEmptyState
import com.example.modulelensmobile.ui.components.ModuleLensErrorState
import com.example.modulelensmobile.ui.components.ModuleLensInlineError
import com.example.modulelensmobile.ui.components.ModuleLensLoadingState
import com.example.modulelensmobile.ui.components.ModuleLensRefreshingIndicator
import com.example.modulelensmobile.ui.components.ModuleLensTopBar
import com.example.modulelensmobile.ui.components.StatusChip

@Composable
fun BoardNotesScreen(
    viewModel: BoardNotesViewModel,
    onNavigateToOcrResult: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ModuleLensTopBar(
                title = "Board Notes",
                actions = {
                    IconButton(onClick = viewModel::loadBoardScans) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh board notes"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                ModuleLensLoadingState(
                    message = "Loading board notes...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            uiState.boardScans.isEmpty() && uiState.errorMessage != null -> {
                ModuleLensErrorState(
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
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
@Composable
private fun BoardNotesContent(
    uiState: BoardNotesUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onNavigateToOcrResult: (String) -> Unit,
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
                ModuleLensInlineError(message = uiState.errorMessage, onRetry = onRetry)
            }
        }

        if (uiState.isRefreshing) {
            item {
                ModuleLensRefreshingIndicator()
            }
        }

        if (uiState.boardScans.isEmpty()) {
            item {
                ModuleLensEmptyState(text = "No board notes saved yet.")
            }
        } else {
            items(uiState.boardScans, key = { it.id }) { scan ->
                BoardScanCard(
                    scan = scan,
                    onClick = { onNavigateToOcrResult(scan.id) }
                )
            }
        }
    }
}
@Composable
private fun BoardScanCard(
    scan: BoardScan,
    onClick: () -> Unit
) {
    ModuleLensCard(onClick = onClick) {
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
                StatusChip(status = scan.reviewStatus)
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
        }
    }
}
