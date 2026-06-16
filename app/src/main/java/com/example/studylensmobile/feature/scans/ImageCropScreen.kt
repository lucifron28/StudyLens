package com.example.studylensmobile.feature.scans

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.studylensmobile.core.ocr.CropArea
import com.example.studylensmobile.core.ocr.ImageCropper
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensInlineError
import com.example.studylensmobile.ui.components.StudyLensTopBar
import kotlinx.coroutines.launch

@Composable
fun ImageCropScreen(
    imageUri: String,
    onBack: () -> Unit,
    onCropConfirmed: (Uri) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cropSize by remember { mutableFloatStateOf(0.8f) }
    var horizontalOffset by remember { mutableFloatStateOf(0f) }
    var verticalOffset by remember { mutableFloatStateOf(0f) }
    var isCropping by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val maxOffset = (1f - cropSize) / 2f

    LaunchedEffect(cropSize) {
        horizontalOffset = horizontalOffset.coerceIn(-maxOffset, maxOffset)
        verticalOffset = verticalOffset.coerceIn(-maxOffset, maxOffset)
    }

    val left = (0.5f - cropSize / 2f + horizontalOffset).coerceIn(0f, 1f - cropSize)
    val top = (0.5f - cropSize / 2f + verticalOffset).coerceIn(0f, 1f - cropSize)
    val cropArea = CropArea(
        left = left,
        top = top,
        right = left + cropSize,
        bottom = top + cropSize
    )

    Scaffold(
        topBar = {
            StudyLensTopBar(
                title = "Crop Scan",
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isCropping) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Adjust the crop area before OCR.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            StudyLensCard {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CropPreview(
                        imageUri = imageUri,
                        cropArea = cropArea
                    )

                    CropSlider(
                        label = "Crop size",
                        value = cropSize,
                        valueRange = CropArea.MIN_CROP_FRACTION..0.95f,
                        onValueChange = { cropSize = it }
                    )
                    CropSlider(
                        label = "Horizontal position",
                        value = horizontalOffset,
                        valueRange = -maxOffset..maxOffset,
                        onValueChange = { horizontalOffset = it }
                    )
                    CropSlider(
                        label = "Vertical position",
                        value = verticalOffset,
                        valueRange = -maxOffset..maxOffset,
                        onValueChange = { verticalOffset = it }
                    )
                }
            }

            errorMessage?.let { message ->
                StudyLensInlineError(
                    message = message,
                    onRetry = { errorMessage = null },
                    retryLabel = "Dismiss"
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        isCropping = true
                        errorMessage = null
                        runCatching {
                            ImageCropper.cropToCache(
                                context = context,
                                sourceUri = Uri.parse(imageUri),
                                cropArea = cropArea
                            )
                        }.onSuccess { croppedUri ->
                            onCropConfirmed(croppedUri)
                        }.onFailure { error ->
                            errorMessage = error.message ?: "Could not crop this image. Try again."
                            isCropping = false
                        }
                    }
                },
                enabled = !isCropping,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                if (isCropping) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onSecondary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "Use Cropped Image",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CropPreview(
    imageUri: String,
    cropArea: CropArea
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
    ) {
        AsyncImage(
            model = Uri.parse(imageUri),
            contentDescription = "Captured board image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .offset(
                    x = maxWidth * cropArea.left,
                    y = maxHeight * cropArea.top
                )
                .size(
                    width = maxWidth * (cropArea.right - cropArea.left),
                    height = maxHeight * (cropArea.bottom - cropArea.top)
                )
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(12.dp)
                )
        )
    }
}

@Composable
private fun CropSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange
        )
    }
}
