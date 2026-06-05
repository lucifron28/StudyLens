package com.example.modulelensmobile.feature.scans

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
import com.example.modulelensmobile.ui.components.StatusChip

@Composable
fun BoardNotesScreen() {
    val boardScans = MockData.boardScans

    Scaffold(
        topBar = { ModuleLensTopBar(title = "Board Notes") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            items(boardScans) { scan ->
                ModuleLensCard {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(text = scan.title)
                        Text(text = scan.subjectCode + " - " + scan.dateLabel)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusChip(status = scan.reviewStatus)
                    }
                }
            }
        }
    }
}
