package com.example.studylensmobile.feature.scans

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.studylensmobile.core.ocr.CropArea
import com.example.studylensmobile.core.ocr.ImageCropper
import com.example.studylensmobile.ui.components.StudyLensCard
import com.example.studylensmobile.ui.components.StudyLensInlineError
import com.example.studylensmobile.ui.components.StudyLensTopBar
import kotlin.math.hypot
import kotlinx.coroutines.launch

@Composable
fun ImageCropScreen(
    imageUri: String,
    onBack: () -> Unit,
    onCropConfirmed: (Uri) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cropArea by remember { mutableStateOf(CropArea.DefaultGuide) }
    var isCropping by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                text = "Crop the board area before OCR.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            StudyLensCard {
                CropPreview(
                    imageUri = imageUri,
                    cropArea = cropArea,
                    onCropAreaChange = { cropArea = it },
                    modifier = Modifier.padding(12.dp)
                )
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
    cropArea: CropArea,
    onCropAreaChange: (CropArea) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCropArea by rememberUpdatedState(cropArea)
    val currentOnCropAreaChange by rememberUpdatedState(onCropAreaChange)
    val cornerHitRadiusPx = with(LocalDensity.current) { 44.dp.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitEachGesture {
                    var activeCropArea = currentCropArea
                    var dragMode = CropDragMode.None
                    var lastPinchDistance: Float? = null
                    var keepGoing: Boolean

                    val down = awaitFirstDown(requireUnconsumed = false)
                    dragMode = activeCropArea.dragModeFor(
                        position = down.position,
                        canvasSize = size,
                        cornerHitRadiusPx = cornerHitRadiusPx
                    )

                    do {
                        val event = awaitPointerEvent()
                        val pressedChanges = event.changes.filter { it.pressed }

                        if (pressedChanges.size >= 2) {
                            val distance = pressedChanges.averageDistance()
                            val previousDistance = lastPinchDistance

                            if (previousDistance != null && previousDistance > 0f) {
                                activeCropArea = activeCropArea.zoomedBy(
                                    zoomChange = distance / previousDistance
                                )
                                currentOnCropAreaChange(activeCropArea)
                            }

                            lastPinchDistance = distance
                            pressedChanges.forEach { it.consume() }
                        } else {
                            lastPinchDistance = null

                            pressedChanges.firstOrNull()?.let { change ->
                                val dragDelta = change.positionChange()

                                if (dragDelta != Offset.Zero && dragMode != CropDragMode.None) {
                                    activeCropArea = activeCropArea.draggedBy(
                                        mode = dragMode,
                                        delta = dragDelta,
                                        canvasSize = size
                                    )
                                    currentOnCropAreaChange(activeCropArea)
                                    change.consume()
                                }
                            }
                        }

                        keepGoing = event.changes.any { it.pressed }
                    } while (keepGoing)
                }
            }
    ) {
        AsyncImage(
            model = Uri.parse(imageUri),
            contentDescription = "Captured board image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        CropOverlay(cropArea = cropArea)
    }
}

@Composable
private fun BoxWithConstraintsScope.CropOverlay(cropArea: CropArea) {
    Box(
        modifier = Modifier
            .offset(
                x = maxWidth * cropArea.left,
                y = maxHeight * cropArea.top
            )
            .size(
                width = maxWidth * cropArea.widthFraction,
                height = maxHeight * cropArea.heightFraction
            )
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        CornerHandle(alignment = Alignment.TopStart)
        CornerHandle(alignment = Alignment.TopEnd)
        CornerHandle(alignment = Alignment.BottomStart)
        CornerHandle(alignment = Alignment.BottomEnd)
    }
}

@Composable
private fun BoxScope.CornerHandle(alignment: Alignment) {
    Box(
        modifier = Modifier
            .align(alignment)
            .size(26.dp)
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
    )
}

private enum class CropDragMode {
    None,
    Move,
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight
}

private val CropArea.widthFraction: Float
    get() = right - left

private val CropArea.heightFraction: Float
    get() = bottom - top

private fun CropArea.dragModeFor(
    position: Offset,
    canvasSize: IntSize,
    cornerHitRadiusPx: Float
): CropDragMode {
    val leftPx = left * canvasSize.width
    val topPx = top * canvasSize.height
    val rightPx = right * canvasSize.width
    val bottomPx = bottom * canvasSize.height

    return when {
        position.distanceTo(leftPx, topPx) <= cornerHitRadiusPx -> CropDragMode.TopLeft
        position.distanceTo(rightPx, topPx) <= cornerHitRadiusPx -> CropDragMode.TopRight
        position.distanceTo(leftPx, bottomPx) <= cornerHitRadiusPx -> CropDragMode.BottomLeft
        position.distanceTo(rightPx, bottomPx) <= cornerHitRadiusPx -> CropDragMode.BottomRight
        position.x in leftPx..rightPx && position.y in topPx..bottomPx -> CropDragMode.Move
        else -> CropDragMode.None
    }
}

private fun CropArea.draggedBy(
    mode: CropDragMode,
    delta: Offset,
    canvasSize: IntSize
): CropArea {
    val dx = delta.x / canvasSize.width.coerceAtLeast(1)
    val dy = delta.y / canvasSize.height.coerceAtLeast(1)

    return when (mode) {
        CropDragMode.Move -> movedBy(dx, dy)
        CropDragMode.TopLeft -> copy(
            left = (left + dx).coerceIn(0f, right - CropArea.MIN_CROP_FRACTION),
            top = (top + dy).coerceIn(0f, bottom - CropArea.MIN_CROP_FRACTION)
        )
        CropDragMode.TopRight -> copy(
            right = (right + dx).coerceIn(left + CropArea.MIN_CROP_FRACTION, 1f),
            top = (top + dy).coerceIn(0f, bottom - CropArea.MIN_CROP_FRACTION)
        )
        CropDragMode.BottomLeft -> copy(
            left = (left + dx).coerceIn(0f, right - CropArea.MIN_CROP_FRACTION),
            bottom = (bottom + dy).coerceIn(top + CropArea.MIN_CROP_FRACTION, 1f)
        )
        CropDragMode.BottomRight -> copy(
            right = (right + dx).coerceIn(left + CropArea.MIN_CROP_FRACTION, 1f),
            bottom = (bottom + dy).coerceIn(top + CropArea.MIN_CROP_FRACTION, 1f)
        )
        CropDragMode.None -> this
    }.normalized()
}

private fun CropArea.movedBy(dx: Float, dy: Float): CropArea {
    val newLeft = (left + dx).coerceIn(0f, 1f - widthFraction)
    val newTop = (top + dy).coerceIn(0f, 1f - heightFraction)
    return CropArea(
        left = newLeft,
        top = newTop,
        right = newLeft + widthFraction,
        bottom = newTop + heightFraction
    ).normalized()
}

private fun CropArea.zoomedBy(zoomChange: Float): CropArea {
    if (zoomChange <= 0f) return this

    val newWidth = (widthFraction / zoomChange).coerceIn(CropArea.MIN_CROP_FRACTION, 1f)
    val newHeight = (heightFraction / zoomChange).coerceIn(CropArea.MIN_CROP_FRACTION, 1f)
    val centerX = (left + right) / 2f
    val centerY = (top + bottom) / 2f
    val newLeft = (centerX - newWidth / 2f).coerceIn(0f, 1f - newWidth)
    val newTop = (centerY - newHeight / 2f).coerceIn(0f, 1f - newHeight)

    return CropArea(
        left = newLeft,
        top = newTop,
        right = newLeft + newWidth,
        bottom = newTop + newHeight
    ).normalized()
}

private fun List<androidx.compose.ui.input.pointer.PointerInputChange>.averageDistance(): Float {
    val center = Offset(
        x = sumOf { it.position.x.toDouble() }.toFloat() / size,
        y = sumOf { it.position.y.toDouble() }.toFloat() / size
    )
    return sumOf { it.position.distanceTo(center).toDouble() }.toFloat() / size
}

private fun Offset.distanceTo(x: Float, y: Float): Float {
    return hypot(this.x - x, this.y - y)
}

private fun Offset.distanceTo(other: Offset): Float {
    return distanceTo(other.x, other.y)
}
