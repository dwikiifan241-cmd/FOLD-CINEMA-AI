package com.foldcinema.ai.data

enum class Resolution(val label: String, val width: Int, val height: Int) {
    HD("720p", 1280, 720),
    FULL_HD("1080p", 1920, 1080),
    UHD("4K", 3840, 2160)
}

data class Project(
    val id: String,
    val name: String,
    val resolution: Resolution,
    val fps: Int,
    val videoUri: String?,
    val videoName: String?,
    val videoDurationMs: Long,
    val createdAt: Long
)

data class VideoInfo(
    val displayName: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val frameRate: Float?,
    val sizeBytes: Long?
)
