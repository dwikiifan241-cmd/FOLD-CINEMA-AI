package com.foldcinema.ai.editor

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.foldcinema.ai.data.Clip
import com.foldcinema.ai.data.MediaAsset
import com.foldcinema.ai.data.Project
import com.foldcinema.ai.data.ProjectRepository
import com.foldcinema.ai.data.UriAccess
import com.foldcinema.ai.data.newId
import com.foldcinema.ai.data.toClip
import com.foldcinema.ai.playback.PlaybackEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlaybackIssue(val clipId: String?, val message: String)

class EditorViewModel(
    application: Application,
    private val repository: ProjectRepository,
    initialProject: Project
) : AndroidViewModel(application) {

    val engine = PlaybackEngine(application)

    var project by mutableStateOf(initialProject)
        private set
    var selectedClipId by mutableStateOf<String?>(initialProject.clips.firstOrNull()?.id)
        private set
    var positionMs by mutableLongStateOf(0L)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var isBuffering by mutableStateOf(false)
        private set
    var volume by mutableFloatStateOf(1f)
        private set
    var issue by mutableStateOf<PlaybackIssue?>(null)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var missingClipIds by mutableStateOf<Set<String>>(emptySet())
        private set

    val clips: List<Clip> get() = project.clips
    val totalDurationMs: Long get() = TimelineOps.totalDuration(project.clips)
    val selectedIndex: Int get() = project.clips.indexOfFirst { it.id == selectedClipId }
    val selectedClip: Clip? get() = project.clips.getOrNull(selectedIndex)

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            this@EditorViewModel.isPlaying = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            this@EditorViewModel.isBuffering = playbackState == Player.STATE_BUFFERING
        }

        override fun onPlayerError(error: PlaybackException) {
            val clip = this@EditorViewModel.project.clips
                .getOrNull(this@EditorViewModel.engine.player.currentMediaItemIndex)
            this@EditorViewModel.issue = PlaybackIssue(clip?.id, describeError(error, clip))
        }
    }

    init {
        engine.player.addListener(listener)
        engine.setClips(project.clips, 0L)
        refreshMissing()
        viewModelScope.launch {
            while (true) {
                positionMs = engine.globalPositionMs().coerceIn(0L, totalDurationMs)
                delay(if (isPlaying) 50L else 200L)
            }
        }
    }

    // ---------- playback ----------

    fun togglePlayback() {
        if (totalDurationMs <= 0L) return
        val player = engine.player
        if (player.isPlaying) {
            player.pause()
            return
        }
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        if (player.playbackState == Player.STATE_ENDED || positionMs >= totalDurationMs - 50L) {
            engine.seekToGlobal(0L)
        }
        issue = null
        player.play()
    }

    fun pause() {
        engine.player.pause()
    }

    fun seekTo(globalMs: Long) {
        val target = globalMs.coerceIn(0L, totalDurationMs)
        engine.seekToGlobal(target)
        positionMs = target
    }

    fun selectAndSeek(clipId: String, globalMs: Long) {
        selectedClipId = clipId
        seekTo(globalMs)
    }

    fun updateVolume(value: Float) {
        volume = value.coerceIn(0f, 1f)
        engine.player.volume = volume
    }

    // ---------- editing (all non-destructive) ----------

    fun addClips(assets: List<MediaAsset>) {
        if (assets.isEmpty()) return
        val added = assets.map { it.toClip() }
        val insertAt = if (selectedIndex >= 0) selectedIndex + 1 else clips.size
        val list = clips.toMutableList().also { it.addAll(insertAt, added) }
        commit(list, added.first().id, TimelineOps.startOf(list, insertAt))
        refreshMissing()
    }

    fun splitAtPlayhead() {
        val position = engine.globalPositionMs()
        val where = TimelineOps.locate(clips, position)
        val result = where?.let { TimelineOps.split(clips, it.index, it.offsetMs, newId()) }
        if (where == null || result == null) {
            showMessage("Move the playhead inside a clip, away from its edges, to split.")
            return
        }
        commit(result, clips[where.index].id, position)
    }

    fun trimStartToPlayhead() {
        val clip = selectedClip ?: return
        val clipStart = TimelineOps.startOf(clips, selectedIndex)
        val position = engine.globalPositionMs()
        if (position < clipStart || position > clipStart + clip.durationMs) {
            showMessage("Place the playhead inside the selected clip first.")
            return
        }
        trimSelectedStart(clip.trimStartMs + (position - clipStart))
    }

    fun trimEndToPlayhead() {
        val clip = selectedClip ?: return
        val clipStart = TimelineOps.startOf(clips, selectedIndex)
        val position = engine.globalPositionMs()
        if (position < clipStart || position > clipStart + clip.durationMs) {
            showMessage("Place the playhead inside the selected clip first.")
            return
        }
        trimSelectedEnd(clip.trimStartMs + (position - clipStart))
    }

    /** [sourceMs] is a time in the source video. */
    fun trimSelectedStart(sourceMs: Long) {
        val index = selectedIndex
        if (index < 0) return
        val result = TimelineOps.trimStart(clips, index, sourceMs)
        if (result == null) {
            showMessage("Trim would leave the clip too short.")
            return
        }
        if (result == clips) return
        commit(result, clips[index].id, TimelineOps.startOf(result, index))
    }

    /** [sourceMs] is a time in the source video. */
    fun trimSelectedEnd(sourceMs: Long) {
        val index = selectedIndex
        if (index < 0) return
        val result = TimelineOps.trimEnd(clips, index, sourceMs)
        if (result == null) {
            showMessage("Trim would leave the clip too short.")
            return
        }
        if (result == clips) return
        val clipStart = TimelineOps.startOf(result, index)
        val keep = engine.globalPositionMs().coerceIn(clipStart, clipStart + result[index].durationMs)
        commit(result, clips[index].id, keep)
    }

    fun resetSelectedTrim() {
        val index = selectedIndex
        val result = TimelineOps.resetTrim(clips, index) ?: return
        if (result == clips) return
        commit(result, clips[index].id, TimelineOps.startOf(result, index))
    }

    fun deleteSelected() {
        selectedClip?.let { removeClip(it.id) }
    }

    fun removeClip(clipId: String) {
        val index = clips.indexOfFirst { it.id == clipId }
        if (index < 0) return
        val list = TimelineOps.delete(clips, index)
        val next = list.getOrNull(index) ?: list.lastOrNull()
        val nextIndex = list.indexOfFirst { it.id == next?.id }
        commit(list, next?.id, if (nextIndex >= 0) TimelineOps.startOf(list, nextIndex) else 0L)
        refreshMissing()
    }

    fun removeMissingClips() {
        val missing = missingClipIds
        if (missing.isEmpty()) return
        val list = clips.filterNot { it.id in missing }
        val selected = list.firstOrNull { it.id == selectedClipId } ?: list.firstOrNull()
        commit(list, selected?.id, 0L)
        missingClipIds = emptySet()
    }

    fun moveSelected(delta: Int) {
        val index = selectedIndex
        val result = TimelineOps.move(clips, index, index + delta) ?: return
        commit(result, clips[index].id, TimelineOps.startOf(result, index + delta))
    }

    // ---------- messages ----------

    fun showMessage(text: String) {
        message = text
    }

    fun dismissMessage() {
        message = null
    }

    fun dismissIssue() {
        issue = null
    }

    // ---------- internals ----------

    /** Applies an edit: updates project state, saves it, and rebuilds the playlist. */
    private fun commit(newClips: List<Clip>, newSelectedId: String?, newPositionMs: Long) {
        engine.player.pause()
        project = project.copy(clips = newClips, updatedAt = System.currentTimeMillis())
        selectedClipId = newSelectedId
        issue = null
        repository.updateProject(project)
        engine.setClips(newClips, newPositionMs)
        positionMs = newPositionMs.coerceIn(0L, TimelineOps.totalDuration(newClips))
    }

    private fun refreshMissing() {
        val snapshot = project.clips
        val context = getApplication<Application>()
        viewModelScope.launch {
            val missing = withContext(Dispatchers.IO) {
                snapshot
                    .filter { !UriAccess.canRead(context, Uri.parse(it.sourceUri)) }
                    .map { it.id }
                    .toSet()
            }
            missingClipIds = missing
        }
    }

    private fun describeError(error: PlaybackException, clip: Clip?): String {
        val name = clip?.displayName?.let { "\"$it\"" } ?: "This clip"
        return when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                "$name was not found. It may have been moved or deleted."
            PlaybackException.ERROR_CODE_IO_NO_PERMISSION ->
                "Access to $name was revoked. Import the video again to restore access."
            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
            PlaybackException.ERROR_CODE_DECODING_FAILED,
            PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES,
            PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED ->
                "This device cannot decode $name."
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ->
                "$name is damaged or uses an unsupported file format."
            else -> "Playback failed for $name (${error.errorCodeName})."
        }
    }

    override fun onCleared() {
        engine.player.removeListener(listener)
        engine.release()
    }
}
