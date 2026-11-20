package com.foldcinema.ai.data

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ProjectStore(application)

    var projects by mutableStateOf(store.load())
        private set

    fun createProject(name: String, resolution: Resolution, fps: Int): Project {
        val project = Project(
            id = UUID.randomUUID().toString(),
            name = name,
            resolution = resolution,
            fps = fps,
            videoUri = null,
            videoName = null,
            videoDurationMs = 0L,
            createdAt = System.currentTimeMillis()
        )
        add(project)
        return project
    }

    fun createFromVideo(uri: Uri, info: VideoInfo): Project {
        val longestSide = max(info.width, info.height)
        val resolution = when {
            longestSide >= 3000 -> Resolution.UHD
            longestSide >= 1900 -> Resolution.FULL_HD
            else -> Resolution.HD
        }
        val sourceRate = info.frameRate ?: 30f
        val fps = listOf(24, 30, 60).minByOrNull { abs(it - sourceRate) } ?: 30
        val project = Project(
            id = UUID.randomUUID().toString(),
            name = info.displayName.substringBeforeLast('.').ifBlank { "Imported Project" },
            resolution = resolution,
            fps = fps,
            videoUri = uri.toString(),
            videoName = info.displayName,
            videoDurationMs = info.durationMs,
            createdAt = System.currentTimeMillis()
        )
        add(project)
        return project
    }

    fun find(id: String?): Project? = projects.firstOrNull { it.id == id }

    fun delete(id: String?) {
        if (id == null) return
        projects = projects.filterNot { it.id == id }
        store.save(projects)
    }

    private fun add(project: Project) {
        projects = listOf(project) + projects
        store.save(projects)
    }
}
