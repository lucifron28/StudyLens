package com.example.modulelensmobile.feature.subjects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modulelensmobile.data.mock.MockData
import com.example.modulelensmobile.ui.components.ModuleLensCard
import com.example.modulelensmobile.ui.components.ModuleLensTopBar
import com.example.modulelensmobile.ui.components.ProgressBar

@Composable
fun SubjectsScreen(
    onNavigateToSubjectDetail: (String) -> Unit
) {
    val subjects = MockData.subjects

    Scaffold(
        topBar = { ModuleLensTopBar(title = "Subjects") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            items(subjects) { subject ->
                ModuleLensCard(
                    onClick = { onNavigateToSubjectDetail(subject.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(text = subject.code + " - " + subject.title)
                        Text(text = subject.itemSummary)
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressBar(progress = subject.progressPercentage / 100f)
                    }
                }
            }
        }
    }
}
