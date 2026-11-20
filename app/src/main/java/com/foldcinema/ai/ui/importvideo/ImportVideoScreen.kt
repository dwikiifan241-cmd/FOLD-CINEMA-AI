package com.foldcinema.ai.ui.importvideo

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.foldcinema.ai.data.VideoInfo
import com.foldcinema.ai.data.VideoMetadataReader
import com.foldcinema.ai.ui.adaptive.WidthClass
import com.foldcinema.ai.ui.components.AdaptiveTwoPane
import com.foldcinema.ai.ui.components.CinemaBackground
import com.foldcinema.ai.ui.components.CinemaTopBar
import com.foldcinema.ai.ui.components.EmptyState
import com.foldcinema.ai.ui.components.InfoCard
import com.foldcinema.ai.ui.components.InfoRow
import com.foldcinema.ai.ui.theme.ErrorRed
import com.foldcinema.ai.ui.theme.TextMuted
import com.foldcinema.ai.util.formatDuration
import com.foldcinema.ai.util.formatSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImportVideoScreen(
    widthClass: WidthClass,
    onBack: () -> Unit,
    onCreateProject: (Uri, VideoInfo) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var info by remember { mutableStateOf<VideoInfo?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Provider does not allow persistable grants; access lasts for this session only.
            }
            selectedUri = uri
            info = null
            error = null
            loading = true
            scope.launch {
                val result = withContext(Dispatchers.IO) { VideoMetadataReader.read(context, uri) }
                loading = false
                if (result == null) {
                    error = "This file could not be read as a video."
                } else {
                    info = result
                }
            }
        }
    }

    CinemaBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            CinemaTopBar(title = "Import Video", onBack = onBack)
            AdaptiveTwoPane(
                widthClass = widthClass,
                first = {
                    InfoCard(title = "Source") {
                        Text(
                            text = "Choose a video from your device. FOLD CINEMA AI reads its resolution, duration and frame rate, then creates a project from it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Button(
                            onClick = { launcher.launch(arrayOf("video/*")) },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text(if (selectedUri == null) "Choose video" else "Choose another video")
                        }
                    }
                },
                second = {
                    val current = info
                    val currentUri = selectedUri
                    when {
                        loading -> CircularProgressIndicator()
                        error != null -> Text(
                            text = error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ErrorRed
                        )
                        current != null && currentUri != null -> {
                            InfoCard(title = "Video details") {
                                InfoRow("File", current.displayName)
                                InfoRow("Duration", formatDuration(current.durationMs))
                                InfoRow("Resolution", "${current.width} × ${current.height}")
                                current.frameRate?.let { InfoRow("Frame rate", "${it.toInt()} fps") }
                                current.sizeBytes?.let { InfoRow("Size", formatSize(it)) }
                            }
                            Button(
                                onClick = { onCreateProject(currentUri, current) },
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                Text("Create project from this video")
                            }
                        }
                        else -> EmptyState("No video selected yet.")
                    }
                }
            )
        }
    }
}
