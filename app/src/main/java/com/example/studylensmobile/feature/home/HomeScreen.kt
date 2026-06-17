package com.example.studylensmobile.feature.home

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.domain.model.Dashboard
import com.example.studylensmobile.domain.model.DashboardActivityItem
import com.example.studylensmobile.domain.model.DashboardContinueLearningItem
import com.example.studylensmobile.domain.model.DashboardUpcomingItem
import com.example.studylensmobile.ui.components.StudyLensEmptyState
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensInlineError
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.ProgressBar
import com.example.studylensmobile.ui.components.SectionHeader
import com.example.studylensmobile.ui.components.StatusChip

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboard = uiState.dashboard

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = "StudyLens",
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
                StudyLensLoadingState(
                    message = "Loading dashboard...",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
            dashboard == null -> {
                StudyLensErrorState(
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
                StudyLensInlineError(message = errorMessage, onRetry = onRetry)
            }
        }

        item {
            OverallProgressCard(dashboard = dashboard, isRefreshing = isRefreshing)
        }

        item {
            StatsRow(dashboard = dashboard)
        }

        item {
            SectionHeader(title = "Latest Posts")
        }
        if (dashboard.upcoming.isEmpty()) {
            item {
                StudyLensEmptyState(text = "No posts yet.")
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
                StudyLensEmptyState(text = "Open a module to start tracking reading progress.")
            }
        } else {
            items(dashboard.continueLearning, key = { "continue-${it.id}" }) { item ->
                ContinueLearningCard(item = item)
            }
        }

        item {
            SectionHeader(title = "Recent Activity")
        }
        if (dashboard.recentActivity.isEmpty()) {
            item {
                StudyLensEmptyState(text = "Your recent study activity will appear here.")
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
    StudyLensCard {
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
            value = dashboard.stats.quizzesCompleted.toString(),
            label = "Quizzes",
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
    StudyLensCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UpcomingCard(item: DashboardUpcomingItem) {
    StudyLensCard {
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
                        .joinToString(" - "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            item.postedAt?.takeIf { it.isNotBlank() }?.let { postedAt ->
                StatusChip(status = postedAt)
            }
        }
    }
}

@Composable
private fun ContinueLearningCard(item: DashboardContinueLearningItem) {
    StudyLensCard {
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
