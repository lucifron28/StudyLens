package com.example.studylensmobile.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.ui.res.painterResource
import com.example.studylensmobile.R
import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.ui.components.floatingAnimation
import com.example.studylensmobile.domain.model.Dashboard
import com.example.studylensmobile.domain.model.DashboardActivityItem
import com.example.studylensmobile.domain.model.DashboardBoardScanItem
import com.example.studylensmobile.domain.model.DashboardUpcomingItem
import com.example.studylensmobile.ui.components.StudyLensEmptyState
import com.example.studylensmobile.ui.components.StudyLensErrorState
import com.example.studylensmobile.ui.components.StudyLensInlineError
import com.example.studylensmobile.ui.components.StudyLensLoadingState
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensTopBar
import com.example.studylensmobile.ui.components.SectionHeader
import com.example.studylensmobile.ui.components.StatusChip
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSubjectDetail: (Int) -> Unit = {}
) {
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
                    onNavigateToSubjectDetail = onNavigateToSubjectDetail,
                    onTaskToggle = viewModel::toggleTaskCompletion,
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
    onNavigateToSubjectDetail: (Int) -> Unit,
    onTaskToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isActivityExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (errorMessage != null && !isRefreshing) {
            item {
                StudyLensInlineError(message = errorMessage, onRetry = onRetry)
            }
        }

        item {
            LumiHeroBanner(
                firstName = firstName,
                pendingTasks = dashboard.stats.pendingTasks
            )
        }

        if (dashboard.upcoming.isNotEmpty()) {
            item {
                SectionHeader(title = "Upcoming Tasks")
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    items(dashboard.upcoming, key = { "upcoming-${it.id}" }) { item ->
                        UpcomingTaskCard(
                            item = item, 
                            modifier = Modifier.width(300.dp),
                            onTaskToggle = { onTaskToggle(item.id) },
                            onClick = { item.subjectId?.let { onNavigateToSubjectDetail(it) } }
                        )
                    }
                }
            }
        }

        if (dashboard.recentBoardScans.isNotEmpty()) {
            item {
                SectionHeader(title = "Recent Board Notes")
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) {
                    items(dashboard.recentBoardScans, key = { "scan-${it.id}" }) { item ->
                        BoardScanCard(item = item, modifier = Modifier.width(280.dp))
                    }
                }
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
            item {
                Column(
                    modifier = Modifier.animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val itemsToShow = if (isActivityExpanded) dashboard.recentActivity else dashboard.recentActivity.take(3)
                    itemsToShow.forEach { item ->
                        ActivityRow(item = item)
                    }
                    if (dashboard.recentActivity.size > 3) {
                        TextButton(
                            onClick = { isActivityExpanded = !isActivityExpanded },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text(if (isActivityExpanded) "Show Less" else "See All Activity")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LumiHeroBanner(firstName: String, pendingTasks: Int) {
    StudyLensCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hi, $firstName!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (pendingTasks > 0) "You have $pendingTasks tasks to review. Ready to jump in?" else "Ready to start learning today?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )

            }
            Image(
                painter = painterResource(id = R.drawable.lumi_thinking),
                contentDescription = "Lumi Mascot",
                modifier = Modifier
                    .height(100.dp)
                    .padding(start = 16.dp)
                    .floatingAnimation()
            )
        }
    }
}


@Composable
private fun UpcomingTaskCard(
    item: DashboardUpcomingItem, 
    modifier: Modifier = Modifier,
    onTaskToggle: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    StudyLensCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = item.taskType.ifBlank { item.type }.toDisplayLabel())
                item.dueDate?.let { dueDate ->
                    Text(
                        text = dueDate.toDueLabel(),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.clickable { onTaskToggle() }
            ) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle completion",
                    tint = if (item.isCompleted) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.subjectTitle,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.description.isNotBlank()) {
                        Text(
                            text = item.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardScanCard(item: DashboardBoardScanItem, modifier: Modifier = Modifier) {
    StudyLensCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = item.reviewStatus.toDisplayLabel())
                Text(
                    text = item.createdAt,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            if (item.moduleTitle.isNotBlank()) {
                Text(
                    text = item.moduleTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = item.subjectTitle,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.summary.isNotBlank()) {
                Text(
                    text = item.summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun String.toDueLabel(): String {
    val dueDate = try {
        LocalDate.parse(this)
    } catch (_: DateTimeParseException) {
        return "Due $this"
    }
    val today = LocalDate.now()
    return when (dueDate) {
        today -> "Due today"
        today.plusDays(1) -> "Due tomorrow"
        else -> "Due $this"
    }
}

@Composable
private fun ActivityRow(item: DashboardActivityItem) {
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
                StatusChip(status = item.type.toDisplayLabel())
                Text(
                    text = item.createdAt,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
