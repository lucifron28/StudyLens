package com.example.modulelensmobile.feature.studytools

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modulelensmobile.ui.components.ModuleLensCard
import com.example.modulelensmobile.ui.components.ModuleLensTopBar

@Composable
fun AiSummaryScreen() {
    Scaffold(
        topBar = { ModuleLensTopBar(title = "AI Summary") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            ModuleLensCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Summary card...")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Key takeaways:")
                    Text("- Point 1")
                    Text("- Point 2")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* Navigate */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Generate Quiz")
            }
        }
    }
}
