package com.example.studylensmobile.core.utils

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

fun ContentResolver.displayName(uri: Uri): String {
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (displayNameIndex >= 0 && cursor.moveToFirst()) {
            cursor.getString(displayNameIndex)?.takeIf { it.isNotBlank() }?.let { return it }
        }
    }

    return uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: "module"
}
