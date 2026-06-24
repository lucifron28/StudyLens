package com.example.studylensmobile.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfViewer(
    file: File,
    modifier: Modifier = Modifier
) {
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }

    DisposableEffect(file) {
        try {
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            fileDescriptor = fd
            val renderer = PdfRenderer(fd)
            pdfRenderer = renderer
            pageCount = renderer.pageCount
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        onDispose {
            pdfRenderer?.close()
            fileDescriptor?.close()
        }
    }

    if (pageCount == 0) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Unable to load PDF.")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().background(Color.Gray),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                count = pageCount,
                key = { pageIndex -> "pdf-page-$pageIndex" }
            ) { index ->
                PdfPage(
                    pdfRenderer = pdfRenderer,
                    pageIndex = index,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PdfPage(
    pdfRenderer: PdfRenderer?,
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pdfRenderer, pageIndex) {
        withContext(Dispatchers.IO) {
            if (pdfRenderer != null && pageIndex < pdfRenderer.pageCount) {
                // Ensure synchronization as PdfRenderer is not thread-safe
                synchronized(pdfRenderer) {
                    try {
                        val page = pdfRenderer.openPage(pageIndex)
                        // Create a bitmap with 2x resolution for better clarity
                        val bm = Bitmap.createBitmap(
                            page.width * 2,
                            page.height * 2,
                            Bitmap.Config.ARGB_8888
                        )
                        // Fill white background
                        bm.eraseColor(android.graphics.Color.WHITE)
                        page.render(bm, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        bitmap = bm
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .background(Color.White)
            .aspectRatio(1f / 1.414f), // standard A4 ratio approx
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "PDF Page ${pageIndex + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } ?: CircularProgressIndicator()
    }
}
