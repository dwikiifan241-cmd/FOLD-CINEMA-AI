package com.foldcinema.ai.ui.importvideo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import com.foldcinema.ai.data.MediaAsset
import com.foldcinema.ai.data.MediaImporter
import com.foldcinema.ai.data.ProjectRepository
import com.foldcinema.ai.ui.adaptive.WidthClass
import com.foldcinema.ai.ui.components.AdaptiveTwoPane
import com.foldcinema.ai.ui.components.AssetRow
import com.foldcinema.ai.ui.components.CinemaBackground
import com.foldcinema.ai.ui.components.CinemaTopBar
import com.foldcinema.ai.ui.components.EmptyState
import com.foldcinema.ai.ui.components.InfoCard
import com.foldcinema.ai.ui.components.SectionTitle
import com.foldcinema.ai.ui.theme.Amber
import com.foldcinema.ai.ui.theme.TextMuted
import kotlinx.coroutines.launch

/** Import one or many videos into the media library, then start a project from the selection. */
@Composable
fun ImportVideoScreen(
    widthClass: WidthClass,
    repository: ProjectRepository,
    onBack: () -> Unit,
    onCreateProject: (List<MediaAsset>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var selectedIds by remember { mutableStateOf<List<String>>(emptyList()) } // keeps pick order

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            importing = true
            scope.launch {
                val result = MediaImporter.import(context, repository, uris)
                importing = false
                status = result.summary()
                selectedIds = selectedIds + result.assets.map { it.id }.filter { it !in selectedIds }
            }
        }
    }

    val library = repository.library
    val chosen = selectedIds.mapNotNull { id -> library.firstOrNull { it.id == id } }

    CinemaBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            CinemaTopBar(title = "Import Video", onBack = onBack)
            AdaptiveTwoPane(
                widthClass = widthClass,
                first = {
                    InfoCard(title = "Source") {
                        Text(
                            text = "Pick one or several videos. They stay where they are; FOLD CINEMA AI keeps access and reads their resolution, duration and frame rate.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Button(
                            onClick = { launcher.launch(arrayOf("video/*")) },
                            enabled = !importing,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text(if (importing) "Importing…" else "Import videos")
                        }
                        if (importing) CircularProgressIndicator(color = Amber)
                        status?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                },
                second = {
                    SectionTitle("Imported videos (${library.size})")
                    if (library.isEmpty()) {
                        EmptyState("No videos imported yet.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            library.forEach { asset ->
                                AssetRow(
                                    asset = asset,
                                    checked = asset.id in selectedIds,
                                    onToggle = {
                                        selectedIds = if (asset.id in selectedIds) {
                                            selectedIds - asset.id
                                        } else {
                                            selectedIds + asset.id
                                        }
                                    },
                                    onRemove = {
                                        selectedIds = selectedIds - asset.id
                                        repository.removeFromLibrary(asset)
                                    }
                                )
                            }
                        }
                        Button(
                            onClick = { onCreateProject(chosen) },
                            enabled = chosen.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text(
                                if (chosen.isEmpty()) "Select videos to create a project"
                                else "Create project from ${chosen.size} video(s)"
                            )
                        }
                    }
                }
            )
        }
    }
}
