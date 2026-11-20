package com.foldcinema.ai.data

import android.content.Context
import android.net.Uri

object UriAccess {
    /** True when the URI can still be opened for reading (file exists and permission is valid). */
    fun canRead(context: Context, uri: Uri): Boolean =
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
        } catch (e: Exception) {
            false
        }
}
