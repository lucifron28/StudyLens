package com.example.modulelensmobile.feature.modules

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modulelensmobile.ui.components.ModuleLensCard
import com.example.modulelensmobile.ui.components.ModuleLensTopBar
import com.example.modulelensmobile.ui.components.ProgressBar

@Composable
fun ModuleReaderScreen(
    moduleId: String,
    onNavigateToSummary: () -> Unit
) {
    Scaffold(
        topBar = { ModuleLensTopBar(title = "Module Reader") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Reading Module: $moduleId")
            Spacer(modifier = Modifier.height(16.dp))
            
            ModuleLensCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Readable content card placeholder...")
                    Spacer(modifier = Modifier.height(8.dp))
                    ProgressBar(progress = 0.5f)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToSummary, modifier = Modifier.fillMaxWidth()) {
                Text("Generate AI Summary")
            }
        }
    }
}
