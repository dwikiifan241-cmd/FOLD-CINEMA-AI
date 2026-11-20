package com.foldcinema.ai.data

import java.util.UUID

enum class Resolution(val label: String, val width: Int, val height: Int) {
    HD("720p", 1280, 720),
    FULL_HD("1080p", 1920, 1080),
    UHD("4K", 3840, 2160)
}

/**
 * One piece of a source video placed on the timeline.
 * Non-destructive: the source file is never modified; only the used range
 * [trimStartMs, trimEndMs] of the source is stored.
 */
data class Clip(
    val id: String,
    val sourceUri: String,
    val displayName: String,
    val sourceDurationMs: Long,
    val trimStartMs: Long,
    val trimEndMs: Long
) {
    val durationMs: Long get() = trimEndMs - trimStartMs
}

data class Project(
    val id: String,
    val name: String,
    val resolution: Resolution,
    val fps: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val clips: List<Clip>
) {
    val durationMs: Long get() = clips.sumOf { it.durationMs }
}

/** A video the user imported into the app's media library. */
data class MediaAsset(
    val id: String,
    val uri: String,
    val displayName: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val frameRate: Float?,
    val sizeBytes: Long?,
    val importedAt: Long
)

/** Raw result of reading a video file's metadata. */
data class VideoInfo(
    val displayName: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val frameRate: Float?,
    val sizeBytes: Long?
)

fun newId(): String = UUID.randomUUID().toString()

fun MediaAsset.toClip(): Clip = Clip(
    id = newId(),
    sourceUri = uri,
    displayName = displayName,
    sourceDurationMs = durationMs,
    trimStartMs = 0L,
    trimEndMs = durationMs
)
