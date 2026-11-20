package com.foldcinema.ai.ui.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foldcinema.ai.data.Project
import com.foldcinema.ai.ui.adaptive.WidthClass
import com.foldcinema.ai.ui.components.AdaptiveTwoPane
import com.foldcinema.ai.ui.components.CinemaBackground
import com.foldcinema.ai.ui.components.CinemaTopBar
import com.foldcinema.ai.ui.components.EmptyState
import com.foldcinema.ai.ui.components.InfoCard
import com.foldcinema.ai.ui.components.InfoRow
import com.foldcinema.ai.ui.components.PreviewPlaceholder
import com.foldcinema.ai.ui.theme.ErrorRed
import com.foldcinema.ai.ui.theme.TextMuted
import com.foldcinema.ai.util.formatDate
import com.foldcinema.ai.util.formatDuration

/** Stage 1 placeholder for the editor: shows project data. Player/timeline arrive in later stages. */
@Composable
fun ProjectScreen(
    widthClass: WidthClass,
    project: Project?,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    CinemaBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            CinemaTopBar(title = project?.name ?: "Project", onBack = onBack)
            if (project == null) {
                EmptyState("Project not found.", Modifier.padding(20.dp))
            } else {
                AdaptiveTwoPane(
                    widthClass = widthClass,
                    first = {
                        PreviewPlaceholder("Preview player (Media3 ExoPlayer) arrives in the next stage")
                        InfoCard(title = "Timeline") {
                            Text(
                                text = "Multi-track timeline is planned for a later stage.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    },
                    second = {
                        InfoCard(title = "Project details") {
                            InfoRow("Resolution", "${project.resolution.width} × ${project.resolution.height} (${project.resolution.label})")
                            InfoRow("Frame rate", "${project.fps} fps")
                            InfoRow("Source video", project.videoName ?: "None")
                            if (project.videoDurationMs > 0L) {
                                InfoRow("Duration", formatDuration(project.videoDurationMs))
                            }
                            InfoRow("Created", formatDate(project.createdAt))
                        }
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Delete project", color = ErrorRed)
                        }
                    }
                )
            }
        }
    }
}
