package com.foldcinema.ai.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.foldcinema.ai.data.Project
import com.foldcinema.ai.ui.adaptive.WidthClass
import com.foldcinema.ai.ui.components.ActionCard
import com.foldcinema.ai.ui.components.BrandWordmark
import com.foldcinema.ai.ui.components.CinemaBackground
import com.foldcinema.ai.ui.components.EmptyState
import com.foldcinema.ai.ui.components.ProjectCard
import com.foldcinema.ai.ui.components.SectionTitle
import com.foldcinema.ai.ui.theme.SurfaceHigh
import com.foldcinema.ai.ui.theme.Teal
import com.foldcinema.ai.ui.theme.TextMuted
import com.foldcinema.ai.ui.theme.TextPrimary

@Composable
fun DashboardScreen(
    widthClass: WidthClass,
    projects: List<Project>,
    onNewProject: () -> Unit,
    onImportVideo: () -> Unit,
    onSettings: () -> Unit,
    onOpenProject: (Project) -> Unit
) {
    CinemaBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            DashboardHeader(widthClass)
            if (widthClass == WidthClass.Compact) {
                CompactDashboard(projects, onNewProject, onImportVideo, onSettings, onOpenProject)
            } else {
                WideDashboard(projects, onNewProject, onImportVideo, onSettings, onOpenProject)
            }
        }
    }
}

@Composable
private fun DashboardHeader(widthClass: WidthClass) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        BrandWordmark()
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(SurfaceHigh)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = widthClass.label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Teal
            )
        }
    }
}

@Composable
private fun HeroText() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Your cinema studio, folded.",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Text(
            text = "Start a project or import a clip to begin editing.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}

@Composable
private fun DashboardActions(
    onNewProject: () -> Unit,
    onImportVideo: () -> Unit,
    onSettings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ActionCard(
            title = "New Project",
            subtitle = "Set resolution and frame rate",
            icon = Icons.Default.Add,
            onClick = onNewProject,
            highlighted = true
        )
        ActionCard(
            title = "Import Video",
            subtitle = "Pick a clip from your device",
            icon = Icons.Default.PlayArrow,
            onClick = onImportVideo
        )
        ActionCard(
            title = "Settings",
            subtitle = "Display and engine information",
            icon = Icons.Default.Settings,
            onClick = onSettings
        )
    }
}

@Composable
private fun CompactDashboard(
    projects: List<Project>,
    onNewProject: () -> Unit,
    onImportVideo: () -> Unit,
    onSettings: () -> Unit,
    onOpenProject: (Project) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { HeroText() }
        item { DashboardActions(onNewProject, onImportVideo, onSettings) }
        item {
            Spacer(Modifier.height(6.dp))
            SectionTitle("Recent projects")
        }
        if (projects.isEmpty()) {
            item { EmptyState("No projects yet. Create one or import a video.") }
        } else {
            items(projects, key = { it.id }) { project ->
                ProjectCard(project = project, onClick = { onOpenProject(project) })
            }
        }
    }
}

@Composable
private fun WideDashboard(
    projects: List<Project>,
    onNewProject: () -> Unit,
    onImportVideo: () -> Unit,
    onSettings: () -> Unit,
    onOpenProject: (Project) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.4f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HeroText()
            DashboardActions(onNewProject, onImportVideo, onSettings)
        }
        Column(modifier = Modifier.weight(0.6f)) {
            SectionTitle("Recent projects")
            Spacer(Modifier.height(12.dp))
            if (projects.isEmpty()) {
                EmptyState("No projects yet. Create one or import a video.")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 220.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectCard(project = project, onClick = { onOpenProject(project) })
                    }
                }
            }
        }
    }
}
