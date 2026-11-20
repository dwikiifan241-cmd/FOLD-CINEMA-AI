package com.foldcinema.ai.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.foldcinema.ai.data.Clip
import com.foldcinema.ai.editor.ClipPosition
import com.foldcinema.ai.editor.TimelineOps

/**
 * Plays the timeline as an ExoPlayer playlist: every clip becomes one MediaItem with a
 * ClippingConfiguration, so trims and splits change what is actually played while the source
 * files stay untouched. Owns the player; call [release] exactly once.
 */
class PlaybackEngine(context: Context) {

    val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build(),
            /* handleAudioFocus = */ true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private var clips: List<Clip> = emptyList()

    /** Replaces the playlist and positions the player at [positionMs] on the timeline. */
    fun setClips(newClips: List<Clip>, positionMs: Long) {
        clips = newClips
        if (newClips.isEmpty()) {
            player.clearMediaItems()
            return
        }
        val start = TimelineOps.locate(newClips, positionMs) ?: ClipPosition(0, 0L)
        player.setMediaItems(newClips.map { it.toMediaItem() }, start.index, start.offsetMs)
        player.prepare()
    }

    fun seekToGlobal(positionMs: Long) {
        val target = TimelineOps.locate(clips, positionMs) ?: return
        player.seekTo(target.index, target.offsetMs)
    }

    /** Current position on the whole timeline. */
    fun globalPositionMs(): Long {
        if (clips.isEmpty()) return 0L
        val index = player.currentMediaItemIndex.coerceIn(0, clips.lastIndex)
        val offset = player.currentPosition.coerceIn(0L, clips[index].durationMs)
        return TimelineOps.startOf(clips, index) + offset
    }

    fun release() {
        player.release()
    }
}

private fun Clip.toMediaItem(): MediaItem {
    val clipping = MediaItem.ClippingConfiguration.Builder()
        .setStartPositionMs(trimStartMs)
        .setEndPositionMs(
            // Playing to the real end avoids errors when stored duration differs slightly from the file's.
            if (trimEndMs >= sourceDurationMs) C.TIME_END_OF_SOURCE else trimEndMs
        )
        .build()
    return MediaItem.Builder()
        .setUri(Uri.parse(sourceUri))
        .setMediaId(id)
        .setClippingConfiguration(clipping)
        .build()
}
