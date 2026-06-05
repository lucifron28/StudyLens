package com.example.modulelensmobile.feature.scans

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
fun OcrResultScreen(
    onSaveNote: () -> Unit
) {
    Scaffold(
        topBar = { ModuleLensTopBar(title = "OCR Result") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            ModuleLensCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Image preview placeholder...")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ModuleLensCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Extracted text card...")
                    Text("Linked subject/module fields...")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSaveNote, modifier = Modifier.fillMaxWidth()) {
                Text("Save Note")
            }
        }
    }
}
