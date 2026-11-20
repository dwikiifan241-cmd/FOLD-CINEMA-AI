package com.foldcinema.ai.ui.editor

import android.app.Application
import android.view.LayoutInflater
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.ui.PlayerView
import com.foldcinema.ai.R
import com.foldcinema.ai.data.ProjectRepository
import com.foldcinema.ai.editor.EditorViewModel
import com.foldcinema.ai.ui.adaptive.WidthClass
import com.foldcinema.ai.ui.components.Banner
import com.foldcinema.ai.ui.components.CinemaBackground
import com.foldcinema.ai.ui.components.CinemaTopBar
import com.foldcinema.ai.ui.components.EmptyState
import com.foldcinema.ai.ui.theme.Amber
import com.foldcinema.ai.ui.theme.ErrorRed
import com.foldcinema.ai.ui.theme.SurfaceHigh
import com.foldcinema.ai.ui.theme.TextMuted
import com.foldcinema.ai.ui.theme.TextPrimary
import kotlinx.coroutines.delay

/** Entry point of the project route: waits for the repository, then creates the editor view model once. */
@Composable
fun EditorRoute(
    widthClass: WidthClass,
    projectId: String?,
    repository: ProjectRepository,
    onBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val project = if (repository.isLoaded) repository.findProject(projectId) else null

    CinemaBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            when {
                !repository.isLoaded -> {
                    CinemaTopBar(title = "Editor", onBack = onBack)
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Amber)
                    }
                }
                project == null -> {
                    CinemaTopBar(title = "Project", onBack = onBack)
                    EmptyState("Project not found.", Modifier.padding(20.dp))
                }
                else -> {
                    val vm: EditorViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer { EditorViewModel(application, repository, project) }
                        }
                    )
                    EditorScreen(widthClass, vm, repository, onBack)
                }
            }
        }
    }
}

@Composable
private fun EditorScreen(
    widthClass: WidthClass,
    vm: EditorViewModel,
    repository: ProjectRepository,
    onBack: () -> Unit
) {
    // Pause when the app leaves the foreground.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, vm) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) vm.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    // Keep the screen awake only while playing.
    val view = LocalView.current
    DisposableEffect(vm.isPlaying) {
        view.keepScreenOn = vm.isPlaying
        onDispose { view.keepScreenOn = false }
    }

    var showAdd by remember { mutableStateOf(false) }
    var showTrim by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val wide = widthClass != WidthClass.Compact
    val sidePadding = if (wide) 24.dp else 12.dp
    val selected = vm.selectedClip

    Column(Modifier.fillMaxSize()) {
        CinemaTopBar(title = vm.project.name, onBack = onBack) {
            IconButton(onClick = { showDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete project", tint = TextMuted)
            }
        }

        // Preview
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .padding(horizontal = sidePadding)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (vm.clips.isEmpty()) {
                Text(
                    "Add a clip to start editing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            } else {
                val player = vm.engine.player
                AndroidView(
                    factory = { context ->
                        (LayoutInflater.from(context).inflate(R.layout.view_player, null, false) as PlayerView).also {
                            it.player = player
                        }
                    },
                    update = { it.player = player },
                    onRelease = { it.player = null },
                    modifier = Modifier.fillMaxSize()
                )
                if (vm.isBuffering) {
                    CircularProgressIndicator(color = Amber, modifier = Modifier.size(32.dp))
                }
            }
        }

        PlaybackControls(
            vm = vm,
            widthClass = widthClass,
            modifier = Modifier.padding(horizontal = sidePadding - 4.dp, vertical = 4.dp)
        )

        // Messages
        Column(Modifier.padding(horizontal = sidePadding)) {
            vm.issue?.let { issue ->
                Banner(
                    text = issue.message,
                    isError = true,
                    actionLabel = if (issue.clipId != null) "Remove clip" else null,
                    onAction = { issue.clipId?.let { vm.removeClip(it) } },
                    onDismiss = { vm.dismissIssue() },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            if (vm.missingClipIds.isNotEmpty()) {
                Banner(
                    text = "${vm.missingClipIds.size} clip(s) point to videos that can no longer be opened.",
                    isError = true,
                    actionLabel = "Remove",
                    onAction = { vm.removeMissingClips() },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            val message = vm.message
            if (message != null) {
                LaunchedEffect(message) {
                    delay(3500)
                    vm.dismissMessage()
                }
                Banner(
                    text = message,
                    isError = false,
                    onDismiss = { vm.dismissMessage() },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        if (showTrim && selected != null) {
            TrimPanel(
                clip = selected,
                onTrimStart = { vm.trimSelectedStart(it) },
                onTrimEnd = { vm.trimSelectedEnd(it) },
                onStartAtPlayhead = { vm.trimStartToPlayhead() },
                onEndAtPlayhead = { vm.trimEndToPlayhead() },
                onReset = { vm.resetSelectedTrim() },
                modifier = Modifier.padding(horizontal = sidePadding, vertical = 4.dp)
            )
        }

        TimelineView(
            clips = vm.clips,
            selectedClipId = vm.selectedClipId,
            missingClipIds = vm.missingClipIds,
            positionMs = vm.positionMs,
            baseDpPerSecond = if (wide) 36f else 24f,
            onSelectAndSeek = { id, ms -> vm.selectAndSeek(id, ms) },
            modifier = Modifier
                .padding(horizontal = sidePadding, vertical = 4.dp)
                .height(if (wide) 120.dp else 88.dp)
        )

        EditorToolbar(
            canSplit = vm.clips.isNotEmpty(),
            hasSelection = selected != null,
            canMoveLeft = vm.selectedIndex > 0,
            canMoveRight = vm.selectedIndex >= 0 && vm.selectedIndex < vm.clips.lastIndex,
            trimActive = showTrim && selected != null,
            onAdd = { showAdd = true },
            onSplit = { vm.splitAtPlayhead() },
            onTrim = { showTrim = !showTrim },
            onMoveLeft = { vm.moveSelected(-1) },
            onMoveRight = { vm.moveSelected(1) },
            onDelete = { vm.deleteSelected() }
        )
    }

    if (showAdd) {
        AddClipDialog(
            repository = repository,
            onDismiss = { showAdd = false },
            onAdd = { assets ->
                vm.addClips(assets)
                showAdd = false
            }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor = SurfaceHigh,
            title = { Text("Delete project?", color = TextPrimary) },
            text = {
                Text(
                    "\"${vm.project.name}\" will be removed. Your video files are not deleted.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    val id = vm.project.id
                    onBack()
                    repository.deleteProject(id)
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
        )
    }
}
