package com.example.modulelensmobile.feature.subjects

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modulelensmobile.data.mock.MockData
import com.example.modulelensmobile.ui.components.ModuleLensTopBar

@Composable
fun SubjectDetailScreen(
    subjectId: String,
    onBack: () -> Unit
) {
    val subject = MockData.subjects.find { it.id == subjectId }

    Scaffold(
        topBar = { ModuleLensTopBar(title = subject?.title ?: "Subject Detail") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Details for ${subject?.title}")
            // Placeholder for continue learning, tasks, and recent board notes
            Text("Upcoming tasks...")
            Text("Recent board notes...")
        }
    }
}
