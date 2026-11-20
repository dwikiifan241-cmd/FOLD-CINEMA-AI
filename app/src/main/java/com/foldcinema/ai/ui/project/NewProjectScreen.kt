package com.foldcinema.ai.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foldcinema.ai.data.Resolution
import com.foldcinema.ai.ui.components.CenteredScrollColumn
import com.foldcinema.ai.ui.components.CinemaBackground
import com.foldcinema.ai.ui.components.CinemaTopBar
import com.foldcinema.ai.ui.components.SectionTitle
import com.foldcinema.ai.ui.theme.Amber
import com.foldcinema.ai.ui.theme.CinemaBlack

@Composable
fun NewProjectScreen(
    onBack: () -> Unit,
    onCreate: (name: String, resolution: Resolution, fps: Int) -> Unit
) {
    var name by remember { mutableStateOf("Untitled Project") }
    var resolution by remember { mutableStateOf(Resolution.FULL_HD) }
    var fps by remember { mutableIntStateOf(30) }

    CinemaBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            CinemaTopBar(title = "New Project", onBack = onBack)
            CenteredScrollColumn {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                SectionTitle("Resolution")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Resolution.entries.forEach { option ->
                        FilterChip(
                            selected = option == resolution,
                            onClick = { resolution = option },
                            label = { Text(option.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber,
                                selectedLabelColor = CinemaBlack
                            )
                        )
                    }
                }

                SectionTitle("Frame rate")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(24, 30, 60).forEach { option ->
                        FilterChip(
                            selected = option == fps,
                            onClick = { fps = option },
                            label = { Text("$option fps") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber,
                                selectedLabelColor = CinemaBlack
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onCreate(name.trim().ifEmpty { "Untitled Project" }, resolution, fps)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Create project")
                }
            }
        }
    }
}
