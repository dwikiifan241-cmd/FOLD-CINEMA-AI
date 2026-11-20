package com.foldcinema.ai.editor

import com.foldcinema.ai.data.Clip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TimelineOpsTest {

    private fun clip(id: String, source: Long = 10_000L, start: Long = 0L, end: Long = source) =
        Clip(id, "content://test/$id", id, source, start, end)

    @Test
    fun splitCreatesTwoClipsAndKeepsTotalDuration() {
        val clips = listOf(clip("a"))
        val result = TimelineOps.split(clips, 0, 4_000L, "b")
        assertNotNull(result)
        result!!
        assertEquals(2, result.size)
        assertEquals("a", result[0].id)
        assertEquals(0L, result[0].trimStartMs)
        assertEquals(4_000L, result[0].trimEndMs)
        assertEquals("b", result[1].id)
        assertEquals(4_000L, result[1].trimStartMs)
        assertEquals(10_000L, result[1].trimEndMs)
        assertEquals(10_000L, TimelineOps.totalDuration(result))
    }

    @Test
    fun splitUsesSourceTimeOfTrimmedClip() {
        val clips = listOf(clip("a", start = 2_000L, end = 8_000L))
        val result = TimelineOps.split(clips, 0, 1_000L, "b")!!
        assertEquals(3_000L, result[0].trimEndMs)
        assertEquals(3_000L, result[1].trimStartMs)
        assertEquals(8_000L, result[1].trimEndMs)
    }

    @Test
    fun splitTooCloseToEdgeIsRejected() {
        val clips = listOf(clip("a"))
        assertNull(TimelineOps.split(clips, 0, 100L, "b"))
        assertNull(TimelineOps.split(clips, 0, 9_900L, "b"))
        assertNull(TimelineOps.split(clips, 3, 5_000L, "b"))
    }

    @Test
    fun locateMapsGlobalTimeToClip() {
        val clips = listOf(clip("a", end = 4_000L), clip("b", end = 6_000L))
        assertEquals(ClipPosition(0, 3_999L), TimelineOps.locate(clips, 3_999L))
        assertEquals(ClipPosition(1, 0L), TimelineOps.locate(clips, 4_000L))
        assertEquals(ClipPosition(1, 6_000L), TimelineOps.locate(clips, 10_000L))
        assertEquals(ClipPosition(1, 6_000L), TimelineOps.locate(clips, 99_999L))
        assertNull(TimelineOps.locate(emptyList(), 0L))
    }

    @Test
    fun startOfSumsPreviousDurations() {
        val clips = listOf(clip("a", end = 4_000L), clip("b", end = 6_000L), clip("c", end = 1_000L))
        assertEquals(0L, TimelineOps.startOf(clips, 0))
        assertEquals(4_000L, TimelineOps.startOf(clips, 1))
        assertEquals(10_000L, TimelineOps.startOf(clips, 2))
    }

    @Test
    fun trimStartShortensClipAndRejectsTooShort() {
        val clips = listOf(clip("a"))
        val trimmed = TimelineOps.trimStart(clips, 0, 3_000L)!!
        assertEquals(3_000L, trimmed[0].trimStartMs)
        assertEquals(7_000L, trimmed[0].durationMs)
        assertNull(TimelineOps.trimStart(clips, 0, 9_900L))
        assertEquals(clips, TimelineOps.trimStart(clips, 0, 0L))
    }

    @Test
    fun trimEndShortensClipAndIsClampedToSource() {
        val clips = listOf(clip("a"))
        val trimmed = TimelineOps.trimEnd(clips, 0, 6_000L)!!
        assertEquals(6_000L, trimmed[0].trimEndMs)
        assertEquals(6_000L, trimmed[0].durationMs)
        assertNull(TimelineOps.trimEnd(clips, 0, 100L))
        assertEquals(clips, TimelineOps.trimEnd(clips, 0, 50_000L))
    }

    @Test
    fun resetTrimRestoresFullSource() {
        val clips = listOf(clip("a", start = 2_000L, end = 5_000L))
        val reset = TimelineOps.resetTrim(clips, 0)!!
        assertEquals(0L, reset[0].trimStartMs)
        assertEquals(10_000L, reset[0].trimEndMs)
    }

    @Test
    fun moveReordersClips() {
        val clips = listOf(clip("a"), clip("b"), clip("c"))
        val moved = TimelineOps.move(clips, 0, 1)!!
        assertEquals(listOf("b", "a", "c"), moved.map { it.id })
        assertNull(TimelineOps.move(clips, 0, -1))
        assertNull(TimelineOps.move(clips, 2, 3))
        assertNull(TimelineOps.move(clips, 1, 1))
    }

    @Test
    fun deleteRemovesOnlyThatClip() {
        val clips = listOf(clip("a"), clip("b"), clip("c"))
        assertEquals(listOf("a", "c"), TimelineOps.delete(clips, 1).map { it.id })
    }

    @Test
    fun editsNeverModifyOriginalList() {
        val clips = listOf(clip("a"))
        TimelineOps.split(clips, 0, 4_000L, "b")
        TimelineOps.trimStart(clips, 0, 3_000L)
        TimelineOps.move(clips, 0, 0)
        assertEquals(1, clips.size)
        assertEquals(0L, clips[0].trimStartMs)
        assertEquals(10_000L, clips[0].trimEndMs)
    }
}
