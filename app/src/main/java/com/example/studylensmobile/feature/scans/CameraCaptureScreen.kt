package com.example.studylensmobile.feature.scans

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-screen camera capture screen built with CameraX.
 *
 * Features:
 * - Live camera preview
 * - Capture button with press animation
 * - Front / back camera toggle
 * - Flash on / off toggle
 * - Runtime camera permission handling with rationale
 *
 * After a photo is taken the URI is passed back via [onImageCaptured] so the
 * caller can feed it into [OcrTextRecognizer] for ML Kit text recognition.
 */
@Composable
fun CameraCaptureScreen(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onImageCaptured: (Uri) -> Unit,
    onBack: () -> Unit
) {
    if (hasPermission) {
        CameraContent(
            onImageCaptured = onImageCaptured,
            onBack = onBack
        )
    } else {
        CameraPermissionScreen(
            onRequestPermission = onRequestPermission,
            onBack = onBack
        )
    }
}

// ----- Permission-denied placeholder -----

@Composable
private fun CameraPermissionScreen(
    onRequestPermission: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            tint = Color(0xFF14B8A6),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Camera Permission Required",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "StudyLens needs camera access to scan board notes and documents. " +
                    "Your photos are processed on-device and never shared.",
            color = Color(0xFFCBD5E1),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Camera Access")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Go Back",
            color = Color(0xFF14B8A6),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clickable { onBack() }
        )
    }
}

// ----- Camera preview + capture -----

@Composable
private fun CameraContent(
    onImageCaptured: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashEnabled by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    // Bind camera whenever lensFacing changes
    LaunchedEffect(lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraCapture", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Update flash mode reactively
    LaunchedEffect(flashEnabled) {
        imageCapture.flashMode = if (flashEnabled) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
    }

    // Unbind when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                cameraProvider.unbindAll()
            } catch (_: Exception) { }
        }
    }

    // Capture-button scale animation for tactile feel
    val captureScale by animateFloatAsState(
        targetValue = if (isCapturing) 0.85f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "capture_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Top bar: back + flash
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x99000000))
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Scan Board",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(onClick = { flashEnabled = !flashEnabled }) {
                val flashTint by animateColorAsState(
                    targetValue = if (flashEnabled) Color(0xFFFBBF24) else Color.White,
                    label = "flash_tint"
                )
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (flashEnabled) "Flash on" else "Flash off",
                    tint = flashTint
                )
            }
        }

        // Bottom controls: flip + capture
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x99000000))
                .padding(vertical = 24.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera flip
            IconButton(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0x44FFFFFF))
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch camera",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(40.dp))

            // Capture button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(76.dp)
                    .scale(captureScale)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .clickable(enabled = !isCapturing) {
                        isCapturing = true
                        capturePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            onCaptured = { uri ->
                                isCapturing = false
                                onImageCaptured(uri)
                            },
                            onError = {
                                isCapturing = false
                            }
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            Spacer(modifier = Modifier.width(40.dp))

            // Placeholder to keep capture centred
            Spacer(modifier = Modifier.size(52.dp))
        }

        // Scanning guide overlay
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(280.dp)
                .align(Alignment.Center)
                .border(
                    width = 2.dp,
                    color = Color(0x8814B8A6),
                    shape = RoundedCornerShape(16.dp)
                )
        )
    }
}

// ----- Helper: take photo & save to cache -----

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onCaptured: (Uri) -> Unit,
    onError: () -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        "scan_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onCaptured(Uri.fromFile(photoFile))
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraCapture", "Photo capture failed", exception)
                Toast.makeText(context, "Capture failed. Try again.", Toast.LENGTH_SHORT).show()
                onError()
            }
        }
    )
}
