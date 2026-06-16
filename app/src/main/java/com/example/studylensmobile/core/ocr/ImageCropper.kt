package com.example.studylensmobile.core.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CropArea(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun normalized(): CropArea {
        val safeLeft = left.coerceIn(0f, 1f - MIN_CROP_FRACTION)
        val safeTop = top.coerceIn(0f, 1f - MIN_CROP_FRACTION)
        val safeRight = right.coerceIn(safeLeft + MIN_CROP_FRACTION, 1f)
        val safeBottom = bottom.coerceIn(safeTop + MIN_CROP_FRACTION, 1f)
        return CropArea(safeLeft, safeTop, safeRight, safeBottom)
    }

    companion object {
        const val MIN_CROP_FRACTION = 0.2f
        val DefaultGuide = CropArea(left = 0.1f, top = 0.1f, right = 0.9f, bottom = 0.9f)
    }
}

object ImageCropper {
    suspend fun cropToCache(
        context: Context,
        sourceUri: Uri,
        cropArea: CropArea
    ): Uri = withContext(Dispatchers.IO) {
        val bitmap = loadBitmap(context, sourceUri)
        val crop = cropArea.normalized()

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

    @Suppress("DEPRECATION")
    private fun loadBitmap(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    private const val JPEG_QUALITY = 92
}
