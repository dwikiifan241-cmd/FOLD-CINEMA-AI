package com.foldcinema.ai.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.foldcinema.ai.ui.components.AssetRow
import com.foldcinema.ai.ui.theme.SurfaceHigh
import com.foldcinema.ai.ui.theme.TextMuted
import com.foldcinema.ai.ui.theme.TextPrimary
import kotlinx.coroutines.launch

/** Pick videos from the media library (or import new ones) and add them to the timeline. */
@Composable
fun AddClipDialog(
    repository: ProjectRepository,
    onDismiss: () -> Unit,
    onAdd: (List<MediaAsset>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedIds by remember { mutableStateOf<List<String>>(emptyList()) } // keeps pick order
    var status by remember { mutableStateOf<String?>(null) }
    var importing by remember { mutableStateOf(false) }

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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceHigh,
        title = { Text("Add clips", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { launcher.launch(arrayOf("video/*")) },
                    enabled = !importing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (importing) "Importing…" else "Import videos from device")
                }
                status?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                val library = repository.library
                if (library.isEmpty()) {
                    Text(
                        "No videos in your library yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedIds.isNotEmpty(),
                onClick = {
                    val chosen = selectedIds.mapNotNull { id -> repository.library.firstOrNull { it.id == id } }
                    onAdd(chosen)
                }
            ) { Text("Add ${selectedIds.size}") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
