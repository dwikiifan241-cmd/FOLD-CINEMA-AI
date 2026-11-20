package com.foldcinema.ai.data

import org.json.JSONArray
import org.json.JSONObject

/** JSON (de)serialisation for projects and the media library. Tolerant of bad values. */
object ProjectCodec {

    const val SCHEMA_VERSION = 2

    fun encode(project: Project): String = JSONObject().apply {
        put("schema", SCHEMA_VERSION)
        put("id", project.id)
        put("name", project.name)
        put("resolution", project.resolution.name)
        put("fps", project.fps)
        put("createdAt", project.createdAt)
        put("updatedAt", project.updatedAt)
        put("clips", JSONArray().also { array ->
            project.clips.forEach { clip ->
                array.put(JSONObject().apply {
                    put("id", clip.id)
                    put("sourceUri", clip.sourceUri)
                    put("displayName", clip.displayName)
                    put("sourceDurationMs", clip.sourceDurationMs)
                    put("trimStartMs", clip.trimStartMs)
                    put("trimEndMs", clip.trimEndMs)
                })
            }
        })
    }.toString()

    /** Throws JSONException when the document is structurally broken. */
    fun decode(json: String): Project {
        val o = JSONObject(json)
        val clipsArray = o.optJSONArray("clips") ?: JSONArray()
        val clips = (0 until clipsArray.length()).mapNotNull { i -> decodeClip(clipsArray.getJSONObject(i)) }
        val createdAt = o.optLong("createdAt", 0L)
        return Project(
            id = o.getString("id"),
            name = o.optString("name", "Untitled Project"),
            resolution = parseResolution(o.optString("resolution", "")),
            fps = o.optInt("fps", 30),
            createdAt = createdAt,
            updatedAt = o.optLong("updatedAt", createdAt),
            clips = clips
        )
    }

    private fun decodeClip(o: org.json.JSONObject): Clip? {
        val uri = o.optString("sourceUri", "")
        val source = o.optLong("sourceDurationMs", 0L)
        if (uri.isEmpty() || source <= 0L) return null
        val start = o.optLong("trimStartMs", 0L).coerceIn(0L, source)
        val end = o.optLong("trimEndMs", source).coerceIn(start, source)
        if (end - start <= 0L) return null
        return Clip(
            id = o.optString("id", "").ifEmpty { newId() },
            sourceUri = uri,
            displayName = o.optString("displayName", "video"),
            sourceDurationMs = source,
            trimStartMs = start,
            trimEndMs = end
        )
    }

    /** Stage 1 stored a single optional video per project in SharedPreferences. */
    fun decodeLegacy(o: JSONObject): Project {
        val createdAt = o.optLong("createdAt", 0L)
        val uri = o.optString("videoUri", "")
        val duration = o.optLong("videoDurationMs", 0L)
        val clips = if (uri.isNotEmpty() && duration > 0L) {
            listOf(
                Clip(
                    id = newId(),
                    sourceUri = uri,
                    displayName = o.optString("videoName", "").ifEmpty { "video" },
                    sourceDurationMs = duration,
                    trimStartMs = 0L,
                    trimEndMs = duration
                )
            )
        } else {
            emptyList()
        }
        return Project(
            id = o.getString("id"),
            name = o.optString("name", "Untitled Project"),
            resolution = parseResolution(o.optString("resolution", "")),
            fps = o.optInt("fps", 30),
            createdAt = createdAt,
            updatedAt = createdAt,
            clips = clips
        )
    }

    fun encodeLibrary(assets: List<MediaAsset>): String {
        val array = JSONArray()
        assets.forEach { a ->
            array.put(JSONObject().apply {
                put("id", a.id)
                put("uri", a.uri)
                put("displayName", a.displayName)
                put("durationMs", a.durationMs)
                put("width", a.width)
                put("height", a.height)
                put("frameRate", (a.frameRate ?: -1f).toDouble())
                put("sizeBytes", a.sizeBytes ?: -1L)
                put("importedAt", a.importedAt)
            })
        }
        return JSONObject().put("assets", array).toString()
    }

    fun decodeLibrary(json: String): List<MediaAsset> {
        val array = JSONObject(json).optJSONArray("assets") ?: return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            val o = array.getJSONObject(i)
            val uri = o.optString("uri", "")
            val duration = o.optLong("durationMs", 0L)
            if (uri.isEmpty() || duration <= 0L) {
                null
            } else {
                val rate = o.optDouble("frameRate", -1.0)
                val size = o.optLong("sizeBytes", -1L)
                MediaAsset(
                    id = o.optString("id", "").ifEmpty { newId() },
                    uri = uri,
                    displayName = o.optString("displayName", "video"),
                    durationMs = duration,
                    width = o.optInt("width", 0),
                    height = o.optInt("height", 0),
                    frameRate = if (rate > 0.0) rate.toFloat() else null,
                    sizeBytes = if (size >= 0L) size else null,
                    importedAt = o.optLong("importedAt", 0L)
                )
            }
        }
    }

    private fun parseResolution(name: String): Resolution =
        Resolution.entries.firstOrNull { it.name == name } ?: Resolution.FULL_HD
}
