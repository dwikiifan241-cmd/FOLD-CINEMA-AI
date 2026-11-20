package com.foldcinema.ai.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Minimal persistence for stage 1 (SharedPreferences + JSON). Replaced by Room in a later stage. */
class ProjectStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<Project> {
        val raw = prefs.getString(KEY_PROJECTS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { index -> array.getJSONObject(index).toProject() }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    fun save(projects: List<Project>) {
        val array = JSONArray()
        projects.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_PROJECTS, array.toString()).apply()
    }

    private fun Project.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("resolution", resolution.name)
        put("fps", fps)
        put("videoUri", videoUri ?: "")
        put("videoName", videoName ?: "")
        put("videoDurationMs", videoDurationMs)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toProject(): Project {
        val resolution = try {
            Resolution.valueOf(optString("resolution", Resolution.FULL_HD.name))
        } catch (e: IllegalArgumentException) {
            Resolution.FULL_HD
        }
        return Project(
            id = getString("id"),
            name = optString("name", "Untitled Project"),
            resolution = resolution,
            fps = optInt("fps", 30),
            videoUri = optString("videoUri", "").ifEmpty { null },
            videoName = optString("videoName", "").ifEmpty { null },
            videoDurationMs = optLong("videoDurationMs", 0L),
            createdAt = optLong("createdAt", 0L)
        )
    }

    private companion object {
        const val PREFS_NAME = "fold_cinema_projects"
        const val KEY_PROJECTS = "projects_json"
    }
}
