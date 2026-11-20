package com.foldcinema.ai.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max

/**
 * Holds all projects and the media library. State is observable from Compose and is always
 * mutated on the main thread; disk writes run in order on a single background thread, so the
 * last edit is saved even if the screen that made it is already gone.
 */
class ProjectRepository(context: Context) {

    private val appContext = context.applicationContext
    private val storage = ProjectStorage(appContext)
    private val io = Executors.newSingleThreadExecutor { r -> Thread(r, "fold-cinema-io") }
    private val main = Handler(Looper.getMainLooper())

    var projects by mutableStateOf<List<Project>>(emptyList())
        private set

    var library by mutableStateOf<List<MediaAsset>>(emptyList())
        private set

    var isLoaded by mutableStateOf(false)
        private set

    init {
        io.execute {
            val loadedProjects = storage.loadProjects()
            val loadedLibrary = storage.loadLibrary()
            main.post {
                projects = loadedProjects
                library = loadedLibrary
                isLoaded = true
            }
        }
    }

    fun findProject(id: String?): Project? = projects.firstOrNull { it.id == id }

    fun createProject(
        name: String,
        resolution: Resolution,
        fps: Int,
        clips: List<Clip> = emptyList()
    ): Project {
        val now = System.currentTimeMillis()
        val project = Project(
            id = newId(),
            name = name,
            resolution = resolution,
            fps = fps,
            createdAt = now,
            updatedAt = now,
            clips = clips
        )
        projects = listOf(project) + projects
        io.execute { storage.saveProject(project) }
        return project
    }

    fun createProjectFromAssets(assets: List<MediaAsset>): Project {
        val first = assets.first()
        val longestSide = max(first.width, first.height)
        val resolution = when {
            longestSide >= 3000 -> Resolution.UHD
            longestSide >= 1900 -> Resolution.FULL_HD
            else -> Resolution.HD
        }
        val sourceRate = first.frameRate ?: 30f
        val fps = listOf(24, 30, 60).minByOrNull { abs(it - sourceRate) } ?: 30
        val name = first.displayName.substringBeforeLast('.').ifBlank { "Imported Project" }
        return createProject(name, resolution, fps, assets.map { it.toClip() })
    }

    fun updateProject(project: Project) {
        projects = (listOf(project) + projects.filterNot { it.id == project.id })
            .sortedByDescending { it.updatedAt }
        io.execute { storage.saveProject(project) }
    }

    fun deleteProject(id: String) {
        projects = projects.filterNot { it.id == id }
        io.execute { storage.deleteProject(id) }
    }

    fun addToLibrary(assets: List<MediaAsset>) {
        if (assets.isEmpty()) return
        library = assets + library
        val snapshot = library
        io.execute { storage.saveLibrary(snapshot) }
    }

    fun removeFromLibrary(asset: MediaAsset) {
        library = library.filterNot { it.id == asset.id }
        val snapshot = library
        io.execute { storage.saveLibrary(snapshot) }
        // Give the access grant back unless a project still needs this file.
        val stillUsed = projects.any { p -> p.clips.any { it.sourceUri == asset.uri } }
        if (!stillUsed) {
            try {
                appContext.contentResolver.releasePersistableUriPermission(
                    Uri.parse(asset.uri), Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // grant was never persisted or already released
            }
        }
    }
}
