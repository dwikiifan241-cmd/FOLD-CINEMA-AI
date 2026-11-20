package com.foldcinema.ai.editor

import com.foldcinema.ai.data.Clip

data class ClipPosition(val index: Int, val offsetMs: Long)

/**
 * All edit operations return NEW lists and never touch source files.
 * Time on the timeline ("global") is the sum of the used (trimmed) ranges of the clips in order.
 * An operation that cannot be applied returns null.
 */
object TimelineOps {

    const val MIN_CLIP_MS = 300L

    fun totalDuration(clips: List<Clip>): Long = clips.sumOf { it.durationMs }

    /** Global start time of the clip at [index]. */
    fun startOf(clips: List<Clip>, index: Int): Long {
        var acc = 0L
        val end = index.coerceIn(0, clips.size)
        for (i in 0 until end) acc += clips[i].durationMs
        return acc
    }

    /**
     * Maps a global time to (clip index, offset inside the clip's used range).
     * A boundary belongs to the following clip; times past the end map to the end of the last clip.
     */
    fun locate(clips: List<Clip>, globalMs: Long): ClipPosition? {
        if (clips.isEmpty()) return null
        val target = globalMs.coerceAtLeast(0L)
        var acc = 0L
        for (i in clips.indices) {
            val d = clips[i].durationMs
            if (target < acc + d) return ClipPosition(i, target - acc)
            acc += d
        }
        val last = clips.lastIndex
        return ClipPosition(last, clips[last].durationMs)
    }

    /** Splits the clip at [index] at [offsetMs] (inside its used range). The first half keeps the id. */
    fun split(clips: List<Clip>, index: Int, offsetMs: Long, newClipId: String): List<Clip>? {
        val clip = clips.getOrNull(index) ?: return null
        if (offsetMs < MIN_CLIP_MS || clip.durationMs - offsetMs < MIN_CLIP_MS) return null
        val point = clip.trimStartMs + offsetMs
        val first = clip.copy(trimEndMs = point)
        val second = clip.copy(id = newClipId, trimStartMs = point)
        return clips.toMutableList().also {
            it[index] = first
            it.add(index + 1, second)
        }
    }

    /** Sets where the used range starts, in SOURCE time. Null when it would leave less than MIN_CLIP_MS. */
    fun trimStart(clips: List<Clip>, index: Int, newStartMs: Long): List<Clip>? {
        val clip = clips.getOrNull(index) ?: return null
        val start = newStartMs.coerceAtLeast(0L)
        if (clip.trimEndMs - start < MIN_CLIP_MS) return null
        if (start == clip.trimStartMs) return clips
        return clips.toMutableList().also { it[index] = clip.copy(trimStartMs = start) }
    }

    /** Sets where the used range ends, in SOURCE time. Null when it would leave less than MIN_CLIP_MS. */
    fun trimEnd(clips: List<Clip>, index: Int, newEndMs: Long): List<Clip>? {
        val clip = clips.getOrNull(index) ?: return null
        val end = newEndMs.coerceAtMost(clip.sourceDurationMs)
        if (end - clip.trimStartMs < MIN_CLIP_MS) return null
        if (end == clip.trimEndMs) return clips
        return clips.toMutableList().also { it[index] = clip.copy(trimEndMs = end) }
    }

    fun resetTrim(clips: List<Clip>, index: Int): List<Clip>? {
        val clip = clips.getOrNull(index) ?: return null
        return clips.toMutableList().also {
            it[index] = clip.copy(trimStartMs = 0L, trimEndMs = clip.sourceDurationMs)
        }
    }

    fun move(clips: List<Clip>, from: Int, to: Int): List<Clip>? {
        if (from !in clips.indices || to !in clips.indices || from == to) return null
        return clips.toMutableList().also {
            val clip = it.removeAt(from)
            it.add(to, clip)
        }
    }

    fun delete(clips: List<Clip>, index: Int): List<Clip> =
        clips.filterIndexed { i, _ -> i != index }
}
