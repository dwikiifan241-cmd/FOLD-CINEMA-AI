package com.foldcinema.ai.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ImportResult(
    /** Every picked video that is now in the library (new and already-known ones), in pick order. */
    val assets: List<MediaAsset>,
    val newCount: Int,
    val duplicateCount: Int,
    val failedCount: Int,
    /** Videos whose access cannot be kept after the app restarts. */
    val temporaryCount: Int
) {
    fun summary(): String {
        val parts = mutableListOf<String>()
        if (newCount > 0) parts += "$newCount imported"
        if (duplicateCount > 0) parts += "$duplicateCount already in library"
        if (failedCount > 0) parts += "$failedCount could not be read as video"
        var text = if (parts.isEmpty()) "Nothing imported." else parts.joinToString(" · ")
        if (temporaryCount > 0) {
            text += ". $temporaryCount file(s) may become inaccessible after restart; pick them with the file browser to keep access."
        }
        return text
    }
}

object MediaImporter {

    /** Call from the main thread. File work runs on Dispatchers.IO; the library is updated on the caller. */
    suspend fun import(context: Context, repository: ProjectRepository, uris: List<Uri>): ImportResult {
        val known = repository.library
        val outcome = withContext(Dispatchers.IO) {
            val resolved = mutableListOf<MediaAsset>()
            val fresh = mutableListOf<MediaAsset>()
            var duplicates = 0
            var failed = 0
            var temporary = 0
            for (uri in uris) {
                val existing = (known + fresh).firstOrNull { it.uri == uri.toString() }
                if (existing != null) {
                    duplicates++
                    resolved += existing
                    continue
                }
                var persisted = true
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    persisted = false
                }
                val info = VideoMetadataReader.read(context, uri)
                if (info == null || info.durationMs <= 0L) {
                    failed++
                    continue
                }
                if (!persisted) temporary++
                val asset = MediaAsset(
                    id = newId(),
                    uri = uri.toString(),
                    displayName = info.displayName,
                    durationMs = info.durationMs,
                    width = info.width,
                    height = info.height,
                    frameRate = info.frameRate,
                    sizeBytes = info.sizeBytes,
                    importedAt = System.currentTimeMillis()
                )
                fresh += asset
                resolved += asset
            }
            ImportResult(resolved, fresh.size, duplicates, failed, temporary) to fresh
        }
        repository.addToLibrary(outcome.second.reversed()) // newest first in the library list
        return outcome.first
    }
}
