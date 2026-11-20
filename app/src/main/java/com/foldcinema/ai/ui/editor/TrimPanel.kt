package com.foldcinema.ai.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.foldcinema.ai.data.Clip
import com.foldcinema.ai.editor.TimelineOps
import com.foldcinema.ai.ui.components.SectionTitle
import com.foldcinema.ai.ui.theme.Amber
import com.foldcinema.ai.ui.theme.OutlineDark
import com.foldcinema.ai.ui.theme.SurfaceHigh
import com.foldcinema.ai.ui.theme.TextMuted
import com.foldcinema.ai.ui.theme.TextPrimary
import com.foldcinema.ai.util.formatPrecise

/** Trim controls for one clip. Sliders work in SOURCE time and commit when released. */
@Composable
fun TrimPanel(
    clip: Clip,
    onTrimStart: (Long) -> Unit,
    onTrimEnd: (Long) -> Unit,
    onStartAtPlayhead: () -> Unit,
    onEndAtPlayhead: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startF by remember(clip.id, clip.trimStartMs) { mutableFloatStateOf(clip.trimStartMs.toFloat()) }
    var endF by remember(clip.id, clip.trimEndMs) { mutableFloatStateOf(clip.trimEndMs.toFloat()) }
    val minGap = TimelineOps.MIN_CLIP_MS.toFloat()
    val source = clip.sourceDurationMs.toFloat()
    val colors = SliderDefaults.colors(
        thumbColor = Amber,
        activeTrackColor = Amber,
        inactiveTrackColor = OutlineDark
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceHigh, contentColor = TextPrimary)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                SectionTitle("Trim")
                Text(
                    text = clip.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Start " + formatPrecise(startF.toLong()), style = MaterialTheme.typography.bodySmall)
                Text("Length " + formatPrecise((endF - startF).toLong()), style = MaterialTheme.typography.bodySmall, color = Amber)
                Text("End " + formatPrecise(endF.toLong()), style = MaterialTheme.typography.bodySmall)
            }
            Slider(
                value = startF,
                onValueChange = { startF = it.coerceAtMost(endF - minGap) },
                onValueChangeFinished = { onTrimStart(startF.toLong()) },
                valueRange = 0f..source,
                colors = colors
            )
            Slider(
                value = endF,
                onValueChange = { endF = it.coerceAtLeast(startF + minGap) },
                onValueChangeFinished = { onTrimEnd(endF.toLong()) },
                valueRange = 0f..source,
                colors = colors
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onStartAtPlayhead,
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("Start = playhead", style = MaterialTheme.typography.bodySmall) }
                OutlinedButton(
                    onClick = onEndAtPlayhead,
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("End = playhead", style = MaterialTheme.typography.bodySmall) }
                TextButton(onClick = onReset, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Reset", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
