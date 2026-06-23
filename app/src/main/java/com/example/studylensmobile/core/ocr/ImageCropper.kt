package com.example.studylensmobile.core.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CropArea(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun normalized(minCropFraction: Float = MIN_CROP_FRACTION): CropArea {
        val safeLeft = left.coerceIn(0f, 1f - minCropFraction)
        val safeTop = top.coerceIn(0f, 1f - minCropFraction)
        val safeRight = right.coerceIn(safeLeft + minCropFraction, 1f)
        val safeBottom = bottom.coerceIn(safeTop + minCropFraction, 1f)
        return CropArea(safeLeft, safeTop, safeRight, safeBottom)
    }

    companion object {
        const val MIN_CROP_FRACTION = 0.2f
        val DefaultGuide = CropArea(left = 0.1f, top = 0.1f, right = 0.9f, bottom = 0.9f)
    }
}

data class ImageCropTransform(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) {
    fun normalized(): ImageCropTransform {
        val safeScale = scale.coerceIn(MIN_SCALE, MAX_SCALE)
        val maxOffset = (safeScale - 1f) / 2f
        return ImageCropTransform(
            scale = safeScale,
            offsetX = offsetX.coerceIn(-maxOffset, maxOffset),
            offsetY = offsetY.coerceIn(-maxOffset, maxOffset)
        )
    }

    companion object {
        const val MIN_SCALE = 1f
        const val MAX_SCALE = 4f
    }
}

object ImageCropper {
    suspend fun cropToCache(
        context: Context,
        sourceUri: Uri,
        cropArea: CropArea,
        imageTransform: ImageCropTransform = ImageCropTransform()
    ): Uri = withContext(Dispatchers.IO) {
        val bitmap = loadBitmap(context, sourceUri)
        val crop = cropArea.toBitmapCrop(
            bitmapWidth = bitmap.width,
            bitmapHeight = bitmap.height,
            imageTransform = imageTransform.normalized()
        )

        val leftPx = (bitmap.width * crop.left).toInt().coerceIn(0, bitmap.width - 1)
        val topPx = (bitmap.height * crop.top).toInt().coerceIn(0, bitmap.height - 1)
        val rightPx = (bitmap.width * crop.right).toInt().coerceIn(leftPx + 1, bitmap.width)
        val bottomPx = (bitmap.height * crop.bottom).toInt().coerceIn(topPx + 1, bitmap.height)

        val cropWidth = max(1, rightPx - leftPx)
        val cropHeight = max(1, bottomPx - topPx)
        val croppedBitmap = Bitmap.createBitmap(bitmap, leftPx, topPx, cropWidth, cropHeight)

        val cropDirectory = File(context.cacheDir, "ocr_crops").apply { mkdirs() }
        val cropFile = File(cropDirectory, "crop_${System.currentTimeMillis()}.jpg")
        cropFile.outputStream().use { output ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        }

        if (croppedBitmap != bitmap) {
            croppedBitmap.recycle()
        }
        bitmap.recycle()

        Uri.fromFile(cropFile)
    }

    private fun loadBitmap(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, imageInfo, _ ->
                val target = targetDimensions(imageInfo.size.width, imageInfo.size.height)
                decoder.setTargetSize(target.width, target.height)
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
        } else {
            loadSampledBitmap(context, uri)
        }
    }

    private fun loadSampledBitmap(context: Context, uri: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("Unable to read the selected image.")
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        } ?: throw IOException("Unable to decode the selected image.")
    }

    private fun targetDimensions(width: Int, height: Int): ImageDimensions {
        val largestDimension = max(width, height)
        if (largestDimension <= MAX_DECODE_DIMENSION) {
            return ImageDimensions(width, height)
        }

        val scale = MAX_DECODE_DIMENSION.toFloat() / largestDimension
        return ImageDimensions(
            width = (width * scale).toInt().coerceAtLeast(1),
            height = (height * scale).toInt().coerceAtLeast(1)
        )
    }

    private fun sampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        while (max(width / sampleSize, height / sampleSize) > MAX_DECODE_DIMENSION) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun CropArea.toBitmapCrop(
        bitmapWidth: Int,
        bitmapHeight: Int,
        imageTransform: ImageCropTransform
    ): CropArea {
        val aspectRatio = bitmapWidth.toFloat() / bitmapHeight.toFloat()
        val baseWidth = if (aspectRatio >= 1f) aspectRatio else 1f
        val baseHeight = if (aspectRatio >= 1f) 1f else 1f / aspectRatio
        val displayedWidth = baseWidth * imageTransform.scale
        val displayedHeight = baseHeight * imageTransform.scale
        val imageLeft = 0.5f - displayedWidth / 2f + imageTransform.offsetX
        val imageTop = 0.5f - displayedHeight / 2f + imageTransform.offsetY

        fun mapX(value: Float): Float {
            return ((value - imageLeft) / displayedWidth).coerceIn(0f, 1f)
        }

        fun mapY(value: Float): Float {
            return ((value - imageTop) / displayedHeight).coerceIn(0f, 1f)
        }

        return CropArea(
            left = mapX(left),
            top = mapY(top),
            right = mapX(right),
            bottom = mapY(bottom)
        ).normalized(minCropFraction = MIN_BITMAP_CROP_FRACTION)
    }

    private const val JPEG_QUALITY = 92
    private const val MIN_BITMAP_CROP_FRACTION = 0.01f
    private const val MAX_DECODE_DIMENSION = 2048

    private data class ImageDimensions(
        val width: Int,
        val height: Int
    )
}
