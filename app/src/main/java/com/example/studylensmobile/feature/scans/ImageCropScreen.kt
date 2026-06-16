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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.studylensmobile.core.ocr.CropArea
import com.example.studylensmobile.core.ocr.ImageCropper
import com.example.studylensmobile.core.ocr.ImageCropTransform
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
    var imageTransform by remember { mutableStateOf(ImageCropTransform()) }
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
                    imageTransform = imageTransform,
                    onCropAreaChange = { cropArea = it },
                    onImageTransformChange = { imageTransform = it },
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
                                cropArea = cropArea,
                                imageTransform = imageTransform
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
    imageTransform: ImageCropTransform,
    onCropAreaChange: (CropArea) -> Unit,
    onImageTransformChange: (ImageCropTransform) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCropArea by rememberUpdatedState(cropArea)
    val currentImageTransform by rememberUpdatedState(imageTransform)
    val currentOnCropAreaChange by rememberUpdatedState(onCropAreaChange)
    val currentOnImageTransformChange by rememberUpdatedState(onImageTransformChange)
    val cornerHitRadiusPx = with(LocalDensity.current) { 36.dp.toPx() }
    var previewSize by remember { mutableStateOf(IntSize.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onSizeChanged { previewSize = it }
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitEachGesture {
                    var activeCropArea = currentCropArea
                    var activeImageTransform = currentImageTransform
                    var dragMode = CropDragMode.None
                    var lastPinchDistance: Float? = null
                    var lastPinchCenter: Offset? = null
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
                            val pinchCenter = pressedChanges.center()
                            val previousDistance = lastPinchDistance
                            val previousPinchCenter = lastPinchCenter

                            if (
                                previousDistance != null &&
                                previousDistance > 0f &&
                                previousPinchCenter != null
                            ) {
                                val centerDelta = Offset(
                                    x = pinchCenter.x - previousPinchCenter.x,
                                    y = pinchCenter.y - previousPinchCenter.y
                                )
                                activeImageTransform = activeImageTransform
                                    .zoomedAround(
                                        zoomChange = distance / previousDistance,
                                        focus = pinchCenter,
                                        canvasSize = size
                                    )
                                    .movedBy(
                                        dx = centerDelta.x / size.width.coerceAtLeast(1),
                                        dy = centerDelta.y / size.height.coerceAtLeast(1)
                                    )
                                currentOnImageTransformChange(activeImageTransform)
                            }

                            lastPinchDistance = distance
                            lastPinchCenter = pinchCenter
                            pressedChanges.forEach { it.consume() }
                        } else {
                            lastPinchDistance = null
                            lastPinchCenter = null

                            pressedChanges.firstOrNull()?.let { change ->
                                val dragDelta = change.positionChange()

                                if (dragDelta != Offset.Zero) {
                                    if (dragMode != CropDragMode.None) {
                                        activeCropArea = activeCropArea.draggedBy(
                                            mode = dragMode,
                                            delta = dragDelta,
                                            canvasSize = size
                                        )
                                        currentOnCropAreaChange(activeCropArea)
                                    } else if (activeImageTransform.scale > ImageCropTransform.MIN_SCALE) {
                                        activeImageTransform = activeImageTransform.movedBy(
                                            dx = dragDelta.x / size.width.coerceAtLeast(1),
                                            dy = dragDelta.y / size.height.coerceAtLeast(1)
                                        )
                                        currentOnImageTransformChange(activeImageTransform)
                                    }
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
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = imageTransform.scale
                    scaleY = imageTransform.scale
                    translationX = imageTransform.offsetX * previewSize.width
                    translationY = imageTransform.offsetY * previewSize.height
                }
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
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        CornerHandle(
            alignment = Alignment.TopStart,
            xOffset = (-8).dp,
            yOffset = (-8).dp
        )
        CornerHandle(
            alignment = Alignment.TopEnd,
            xOffset = 8.dp,
            yOffset = (-8).dp
        )
        CornerHandle(
            alignment = Alignment.BottomStart,
            xOffset = (-8).dp,
            yOffset = 8.dp
        )
        CornerHandle(
            alignment = Alignment.BottomEnd,
            xOffset = 8.dp,
            yOffset = 8.dp
        )
    }
}

@Composable
private fun BoxScope.CornerHandle(
    alignment: Alignment,
    xOffset: Dp,
    yOffset: Dp
) {
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(x = xOffset, y = yOffset)
            .size(16.dp)
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(5.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(5.dp)
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

private fun ImageCropTransform.movedBy(dx: Float, dy: Float): ImageCropTransform {
    return copy(
        offsetX = offsetX + dx,
        offsetY = offsetY + dy
    ).normalized()
}

private fun ImageCropTransform.zoomedAround(
    zoomChange: Float,
    focus: Offset,
    canvasSize: IntSize
): ImageCropTransform {
    if (zoomChange <= 0f) return this

    val oldScale = scale
    val newScale = (scale * zoomChange).coerceIn(
        ImageCropTransform.MIN_SCALE,
        ImageCropTransform.MAX_SCALE
    )
    val scaleRatio = newScale / oldScale
    val focusX = focus.x / canvasSize.width.coerceAtLeast(1) - 0.5f
    val focusY = focus.y / canvasSize.height.coerceAtLeast(1) - 0.5f

    return copy(
        scale = newScale,
        offsetX = focusX + scaleRatio * (offsetX - focusX),
        offsetY = focusY + scaleRatio * (offsetY - focusY)
    ).normalized()
}

private fun List<androidx.compose.ui.input.pointer.PointerInputChange>.averageDistance(): Float {
    val center = Offset(
        x = sumOf { it.position.x.toDouble() }.toFloat() / size,
        y = sumOf { it.position.y.toDouble() }.toFloat() / size
    )
    return sumOf { it.position.distanceTo(center).toDouble() }.toFloat() / size
}

private fun List<androidx.compose.ui.input.pointer.PointerInputChange>.center(): Offset {
    return Offset(
        x = sumOf { it.position.x.toDouble() }.toFloat() / size,
        y = sumOf { it.position.y.toDouble() }.toFloat() / size
    )
}

private fun Offset.distanceTo(x: Float, y: Float): Float {
    return hypot(this.x - x, this.y - y)
}

private fun Offset.distanceTo(other: Offset): Float {
    return distanceTo(other.x, other.y)
}
