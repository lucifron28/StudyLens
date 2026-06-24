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
        // Fix local host issues for emulator mapping to host
        val accessibleUrl = url.replace("127.0.0.1", "10.0.2.2").replace("localhost", "10.0.2.2")
        val filename = accessibleUrl.substringAfterLast("/")
        val file = FileManager.downloadPdf(context, accessibleUrl, filename)
        if (file != null) {
            pdfFile = file
        } else {
            error = "Failed to download PDF."
        }
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
