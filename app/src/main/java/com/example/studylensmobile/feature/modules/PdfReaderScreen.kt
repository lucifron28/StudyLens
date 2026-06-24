package com.example.studylensmobile.feature.modules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.studylensmobile.core.utils.FileManager
import com.example.studylensmobile.ui.components.PdfViewer
import com.example.studylensmobile.ui.components.StudyLensTopBar
import java.io.File

@Composable
fun PdfReaderScreen(
    title: String,
    url: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        val filename = url.substringAfterLast("/")
        FileManager.downloadPdf(context, url, filename)
            .onSuccess { pdfFile = it }
            .onFailure { error = it.message ?: "Failed to download PDF." }
        isLoading = false
    }

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(error!!)
            } else if (pdfFile != null) {
                PdfViewer(file = pdfFile!!)
            }
        }
    }
}
