package com.foldcinema.ai.ui.editor

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foldcinema.ai.editor.EditorViewModel
import com.foldcinema.ai.ui.adaptive.WidthClass
import com.foldcinema.ai.ui.icons.EditorIcons
import com.foldcinema.ai.ui.theme.Amber
import com.foldcinema.ai.ui.theme.OutlineDark
import com.foldcinema.ai.ui.theme.TextMuted
import com.foldcinema.ai.ui.theme.TextPrimary
import com.foldcinema.ai.util.formatDuration

@Composable
fun PlaybackControls(vm: EditorViewModel, widthClass: WidthClass, modifier: Modifier = Modifier) {
    val total = vm.totalDurationMs
    var scrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember { mutableFloatStateOf(0f) }
    val shown = (if (scrubbing) scrubValue else vm.positionMs.toFloat()).coerceIn(0f, maxOf(total, 1L).toFloat())
    val sliderColors = SliderDefaults.colors(
        thumbColor = Amber,
        activeTrackColor = Amber,
        inactiveTrackColor = OutlineDark
    )

    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { vm.togglePlayback() }, enabled = total > 0L) {
                Icon(
                    imageVector = if (vm.isPlaying) EditorIcons.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (vm.isPlaying) "Pause" else "Play",
                    tint = if (total > 0L) Amber else TextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = formatDuration(shown.toLong()),
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                modifier = Modifier.width(40.dp)
            )
            Slider(
                value = shown,
                onValueChange = {
                    scrubbing = true
                    scrubValue = it
                    vm.seekTo(it.toLong())
                },
                onValueChangeFinished = { scrubbing = false },
                valueRange = 0f..maxOf(total, 1L).toFloat(),
                enabled = total > 0L,
                colors = sliderColors,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatDuration(total),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(start = 8.dp).width(40.dp)
            )
            if (widthClass != WidthClass.Compact) {
                VolumeControl(vm, sliderColors, Modifier.width(170.dp).padding(start = 12.dp))
            }
        }
        if (widthClass == WidthClass.Compact) {
            VolumeControl(vm, sliderColors, Modifier.fillMaxWidth(0.6f).padding(start = 12.dp))
        }
    }
}

@Composable
private fun VolumeControl(
    vm: EditorViewModel,
    colors: androidx.compose.material3.SliderColors,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = EditorIcons.Volume,
            contentDescription = "Volume",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Slider(
            value = vm.volume,
            onValueChange = { vm.updateVolume(it) },
            valueRange = 0f..1f,
            colors = colors,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
    }
}
