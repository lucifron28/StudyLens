package com.example.modulelensmobile.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modulelensmobile.data.mock.MockData
import com.example.modulelensmobile.ui.components.ModuleLensCard
import com.example.modulelensmobile.ui.components.ModuleLensTopBar
import com.example.modulelensmobile.ui.components.ProgressBar
import com.example.modulelensmobile.ui.components.SectionHeader

@Composable
fun HomeScreen() {
    val dashboard = MockData.dashboard

    Scaffold(
        topBar = { ModuleLensTopBar(title = "ModuleLens") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Hello, ${dashboard.user.fullName}!")
            Spacer(modifier = Modifier.height(16.dp))

            ModuleLensCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Overall Progress")
                    Spacer(modifier = Modifier.height(8.dp))
                    ProgressBar(progress = dashboard.overallProgress / 100f)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Upcoming Tasks")
            dashboard.upcomingTasks.forEach { task ->
                ModuleLensCard {
                    Text(text = "${task.title} - ${task.dueDate}", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
