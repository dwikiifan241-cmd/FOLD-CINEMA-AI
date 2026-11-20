package com.foldcinema.ai.data

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONException
import java.io.File
import java.io.IOException

/**
 * Persists each project as filesDir/projects/<id>.json and the media library as
 * filesDir/library.json. Writes go through AtomicFile so a crash cannot leave a half-written file.
 * All methods are blocking: call them from a background thread.
 */
class ProjectStorage(private val context: Context) {

    private val projectsDir = File(context.filesDir, "projects")
    private val libraryFile = File(context.filesDir, "library.json")

    fun loadProjects(): List<Project> {
        projectsDir.mkdirs()
        migrateStage1IfNeeded()
        val files = projectsDir.listFiles()?.filter { it.isFile && it.name.endsWith(".json") } ?: return emptyList()
        return files
            .mapNotNull { file ->
                try {
                    ProjectCodec.decode(readText(file))
                } catch (e: Exception) {
                    null // skip unreadable/corrupt project instead of crashing the app
                }
            }
            .sortedByDescending { it.updatedAt }
    }

    fun saveProject(project: Project) {
        projectsDir.mkdirs()
        writeText(File(projectsDir, "${project.id}.json"), ProjectCodec.encode(project))
    }

    fun deleteProject(id: String) {
        val file = File(projectsDir, "$id.json")
        AtomicFile(file).delete()
    }

    fun loadLibrary(): List<MediaAsset> {
        if (!libraryFile.exists()) return emptyList()
        return try {
            ProjectCodec.decodeLibrary(readText(libraryFile))
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveLibrary(assets: List<MediaAsset>) {
        writeText(libraryFile, ProjectCodec.encodeLibrary(assets))
    }

    private fun readText(file: File): String = String(AtomicFile(file).readFully(), Charsets.UTF_8)

    private fun writeText(file: File, text: String) {
        val atomic = AtomicFile(file)
        val out = atomic.startWrite()
        try {
            out.write(text.toByteArray(Charsets.UTF_8))
            atomic.finishWrite(out)
        } catch (e: IOException) {
            atomic.failWrite(out)
        }
    }

    /** Stage 1 kept projects in SharedPreferences. Convert them once so no project is lost. */
    private fun migrateStage1IfNeeded() {
        val prefs = context.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(LEGACY_KEY, null) ?: return
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                saveProject(ProjectCodec.decodeLegacy(array.getJSONObject(i)))
            }
        } catch (e: JSONException) {
            // legacy data unreadable; nothing to migrate
        }
        prefs.edit().remove(LEGACY_KEY).apply()
    }

    private companion object {
        const val LEGACY_PREFS = "fold_cinema_projects"
        const val LEGACY_KEY = "projects_json"
    }
}
