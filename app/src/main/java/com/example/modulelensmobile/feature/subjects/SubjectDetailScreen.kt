package com.example.modulelensmobile.feature.subjects

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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.modulelensmobile.domain.model.SubjectBoardScanPreview
import com.example.modulelensmobile.domain.model.SubjectModulePreview
import com.example.modulelensmobile.domain.model.SubjectOverview
import com.example.modulelensmobile.domain.model.SubjectPostPreview
import com.example.modulelensmobile.domain.model.SubjectTaskPreview
import com.example.modulelensmobile.ui.components.ModuleLensCard
import com.example.modulelensmobile.ui.components.ModuleLensEmptyState
import com.example.modulelensmobile.ui.components.ModuleLensErrorState
import com.example.modulelensmobile.ui.components.ModuleLensInlineError
import com.example.modulelensmobile.ui.components.ModuleLensLoadingState
import com.example.modulelensmobile.ui.components.ModuleLensTopBar
import com.example.modulelensmobile.ui.components.ProgressBar
import com.example.modulelensmobile.ui.components.SectionHeader
import com.example.modulelensmobile.ui.components.StatusChip

@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel,
    onBack: () -> Unit,
    onNavigateToModuleReader: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val overview = uiState.overview

    Scaffold(
        topBar = {
            ModuleLensTopBar(
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
                    IconButton(onClick = viewModel::loadOverview) {
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
                ModuleLensLoadingState(
                    message = "Loading subject...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            overview == null -> {
                ModuleLensErrorState(
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
                    errorMessage = uiState.errorMessage,
                    isRefreshing = uiState.isRefreshing,
                    onRetry = viewModel::loadOverview,
                    onNavigateToModuleReader = onNavigateToModuleReader,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
@Composable
private fun SubjectOverviewContent(
    overview: SubjectOverview,
    errorMessage: String?,
    isRefreshing: Boolean,
    onRetry: () -> Unit,
    onNavigateToModuleReader: (String) -> Unit,
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
                ModuleLensInlineError(message = errorMessage, onRetry = onRetry)
            }
        }

        item {
            SectionHeader(title = "Modules")
        }
        if (overview.latestModules.isEmpty()) {
            item {
                ModuleLensEmptyState(text = "No modules added yet.")
            }
        } else {
            items(overview.latestModules, key = { it.id }) { module ->
                ModulePreviewCard(
                    module = module,
                    onClick = { onNavigateToModuleReader(module.id) }
                )
            }
        }

        item {
            SectionHeader(title = "Upcoming Tasks")
        }
        if (overview.upcomingTasks.isEmpty()) {
            item {
                ModuleLensEmptyState(text = "No upcoming tasks for this subject.")
            }
        } else {
            items(overview.upcomingTasks, key = { it.id }) { task ->
                TaskPreviewCard(task = task)
            }
        }

        item {
            SectionHeader(title = "Recent Board Notes")
        }
        if (overview.recentBoardScans.isEmpty()) {
            item {
                ModuleLensEmptyState(text = "No board notes saved for this subject.")
            }
        } else {
            items(overview.recentBoardScans, key = { it.id }) { boardScan ->
                BoardScanPreviewCard(boardScan = boardScan)
            }
        }

        item {
            SectionHeader(title = "Latest Posts")
        }
        if (overview.latestPosts.isEmpty()) {
            item {
                ModuleLensEmptyState(text = "No posts for this subject yet.")
            }
        } else {
            items(overview.latestPosts, key = { it.id }) { post ->
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
    ModuleLensCard {
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
    onClick: () -> Unit
) {
    ModuleLensCard(onClick = onClick) {
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
            StatusChip(status = module.contentType)
        }
    }
}

@Composable
private fun TaskPreviewCard(task: SubjectTaskPreview) {
    ModuleLensCard {
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
    ModuleLensCard {
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
    ModuleLensCard {
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
