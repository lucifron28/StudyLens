package com.example.studylensmobile.core.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object FileManager {
    suspend fun downloadPdf(context: Context, urlString: String, filename: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val cacheDir = File(context.cacheDir, "pdf_cache")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }
                
                val file = File(cacheDir, filename)
                // Return cached version if it exists
                if (file.exists() && file.length() > 0) {
                    return@withContext file
                }
                
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext null
                }
                
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(file)
                
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
