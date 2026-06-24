package com.example.studylensmobile.core.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object FileManager {
    suspend fun downloadPdf(context: Context, urlString: String, filename: String): Result<File> {
        return withContext(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "pdf_cache")
            val file = File(cacheDir, filename)

            try {
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                if (file.isPdfFile()) {
                    return@withContext Result.success(file)
                }
                file.delete()

                val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15_000
                    readTimeout = 30_000
                }
                try {
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw IOException("Server returned HTTP ${connection.responseCode}.")
                    }

                    connection.inputStream.use { input ->
                        FileOutputStream(file).use(input::copyTo)
                    }
                } finally {
                    connection.disconnect()
                }

                if (!file.isPdfFile()) {
                    throw IOException("The downloaded file is not a valid PDF.")
                }

                Result.success(file)
            } catch (e: Exception) {
                file.delete()
                Result.failure(IOException("Unable to download the PDF: ${e.message}", e))
            }
        }
    }

    private fun File.isPdfFile(): Boolean {
        if (!exists() || length() == 0L) return false

        inputStream().use { input ->
            val header = ByteArray(1_024)
            val byteCount = input.read(header)
            return byteCount > 0 && String(header, 0, byteCount, Charsets.US_ASCII).contains("%PDF-")
        }
    }
}
