package com.example.modulelensmobile.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.modulelensmobile.domain.model.Dashboard
import com.example.modulelensmobile.domain.model.DashboardActivityItem
import com.example.modulelensmobile.domain.model.DashboardContinueLearningItem
import com.example.modulelensmobile.domain.model.DashboardUpcomingItem
import com.example.modulelensmobile.ui.components.ModuleLensCard
import com.example.modulelensmobile.ui.components.ModuleLensTopBar
import com.example.modulelensmobile.ui.components.ProgressBar
import com.example.modulelensmobile.ui.components.SectionHeader
import com.example.modulelensmobile.ui.components.StatusChip

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboard = uiState.dashboard

    Scaffold(
        topBar = {
            ModuleLensTopBar(
                title = "ModuleLens",
                actions = {
                    IconButton(onClick = viewModel::loadDashboard) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh dashboard"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingContent(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            dashboard == null -> {
                ErrorContent(
                    message = uiState.errorMessage ?: "Dashboard is unavailable.",
                    onRetry = viewModel::loadDashboard,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            else -> {
                DashboardContent(
                    firstName = uiState.user?.firstName?.takeIf { it.isNotBlank() }
                        ?: uiState.user?.username
                        ?: "Student",
                    dashboard = dashboard,
                    errorMessage = uiState.errorMessage,
                    isRefreshing = uiState.isRefreshing,
                    onRetry = viewModel::loadDashboard,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    firstName: String,
    dashboard: Dashboard,
    errorMessage: String?,
    isRefreshing: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Hi, $firstName!",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Ready to continue your learning?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (errorMessage != null && !isRefreshing) {
            item {
                InlineError(message = errorMessage, onRetry = onRetry)
            }
        }

        item {
            OverallProgressCard(dashboard = dashboard, isRefreshing = isRefreshing)
        }

        item {
            StatsRow(dashboard = dashboard)
        }

        item {
            SectionHeader(title = "Upcoming")
        }
        if (dashboard.upcoming.isEmpty()) {
            item {
                EmptyCard(text = "No upcoming tasks or posts yet.")
            }
        } else {
            items(dashboard.upcoming, key = { "${it.type}-${it.id}" }) { item ->
                UpcomingCard(item = item)
            }
        }

        item {
            SectionHeader(title = "Continue Learning")
        }
        if (dashboard.continueLearning.isEmpty()) {
            item {
                EmptyCard(text = "Open a module to start tracking reading progress.")
            }
        } else {
            items(dashboard.continueLearning, key = { it.id }) { item ->
                ContinueLearningCard(item = item)
            }
        }

        item {
            SectionHeader(title = "Recent Activity")
        }
        if (dashboard.recentActivity.isEmpty()) {
            item {
                EmptyCard(text = "Your recent study activity will appear here.")
            }
        } else {
            items(dashboard.recentActivity, key = { "${it.type}-${it.id}" }) { item ->
                ActivityRow(item = item)
            }
        }
    }
}

@Composable
private fun OverallProgressCard(
    dashboard: Dashboard,
    isRefreshing: Boolean
) {
    ModuleLensCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Progress",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Mastery: ${dashboard.overallProgress}%",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            ProgressBar(progress = dashboard.overallProgress.coerceIn(0, 100) / 100f)
            Text(
                text = if (isRefreshing) "Refreshing your latest progress..." else "You're on track for your weekly goals.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun StatsRow(dashboard: Dashboard) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            value = dashboard.stats.modulesInProgress.toString(),
            label = "Reading",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = dashboard.stats.notesSaved.toString(),
            label = "Notes",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = dashboard.stats.pendingTasks.toString(),
            label = "Tasks",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    ModuleLensCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun UpcomingCard(item: DashboardUpcomingItem) {
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
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOf(item.subjectTitle, item.type.toDisplayLabel())
                        .filter { it.isNotBlank() }
                        .joinToString(" • "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val chipText = item.dueAt ?: item.postedAt ?: item.status.ifBlank { item.priority }
            if (!chipText.isNullOrBlank()) {
                StatusChip(status = chipText)
            }
        }
    }
}

@Composable
private fun ContinueLearningCard(item: DashboardContinueLearningItem) {
    ModuleLensCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (item.chapterTitle.isNotBlank()) {
                    StatusChip(status = item.chapterTitle)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = item.moduleTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Last read ${item.lastReadAt}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProgressBar(progress = item.progressPercentage.coerceIn(0, 100) / 100f)
            }
            Text(
                text = "${item.progressPercentage}%",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActivityRow(item: DashboardActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusChip(status = item.type.toDisplayLabel())
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = item.createdAt,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text(
            text = "Loading dashboard...",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun InlineError(
    message: String,
    onRetry: () -> Unit
) {
    ModuleLensCard {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyCard(text: String) {
    ModuleLensCard {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun String.toDisplayLabel(): String {
    return split("_")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}
