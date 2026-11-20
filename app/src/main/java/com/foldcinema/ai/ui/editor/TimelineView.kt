package com.foldcinema.ai.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.foldcinema.ai.data.Clip
import com.foldcinema.ai.editor.TimelineOps
import com.foldcinema.ai.ui.theme.Amber
import com.foldcinema.ai.ui.theme.ErrorRed
import com.foldcinema.ai.ui.theme.SurfaceDark
import com.foldcinema.ai.ui.theme.TextMuted
import com.foldcinema.ai.ui.theme.TextPrimary
import com.foldcinema.ai.util.formatDuration
import kotlin.math.min

private val blockColors = listOf(
    Color(0xFF2B3A55), Color(0xFF3A2B55), Color(0xFF2B5547), Color(0xFF55402B)
)

/**
 * Horizontally scrolling multi-clip timeline. Block width is exactly proportional to the clip's
 * used duration, so the playhead position maps linearly onto the timeline.
 */
@Composable
fun TimelineView(
    clips: List<Clip>,
    selectedClipId: String?,
    missingClipIds: Set<String>,
    positionMs: Long,
    baseDpPerSecond: Float,
    onSelectAndSeek: (clipId: String, globalMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SurfaceDark.copy(alpha = 0.7f))
    ) {
        if (clips.isEmpty()) {
            Text(
                text = "Timeline is empty. Tap Add to insert a clip.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.align(Alignment.Center)
            )
            return@Box
        }

        val density = LocalDensity.current
        val totalSeconds = (TimelineOps.totalDuration(clips) / 1000f).coerceAtLeast(1f)
        // Cap total width so very long projects stay within Compose's layout size limit.
        val dpPerSecond = min(baseDpPerSecond, 40_000f / totalSeconds)
        val dpPerMs = dpPerSecond / 1000f
        val scroll = rememberScrollState()
        var viewportPx by remember { mutableIntStateOf(0) }
        val playheadDp: Dp = (positionMs * dpPerMs).dp
        val playheadPx = with(density) { playheadDp.toPx() }

        // Keep the playhead in view while playing or seeking.
        LaunchedEffect(positionMs, viewportPx) {
            if (viewportPx > 0) {
                val left = scroll.value.toFloat()
                if (playheadPx < left || playheadPx > left + viewportPx - 24f) {
                    scroll.scrollTo((playheadPx - viewportPx * 0.3f).toInt().coerceAtLeast(0))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .onSizeChanged { viewportPx = it.width }
                .horizontalScroll(scroll)
        ) {
            Row(Modifier.fillMaxHeight().padding(vertical = 8.dp)) {
                clips.forEach { clip ->
                    ClipBlock(
                        clip = clip,
                        colorIndex = clips.indexOf(clip),
                        width = (clip.durationMs * dpPerMs).dp,
                        selected = clip.id == selectedClipId,
                        missing = clip.id in missingClipIds,
                        onTap = { localMs ->
                            val start = TimelineOps.startOf(clips, clips.indexOf(clip))
                            onSelectAndSeek(clip.id, start + localMs)
                        }
                    )
                }
            }
            // Playhead
            Box(
                Modifier
                    .offset(x = playheadDp)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Amber)
            )
        }
    }
}

@Composable
private fun ClipBlock(
    clip: Clip,
    colorIndex: Int,
    width: Dp,
    selected: Boolean,
    missing: Boolean,
    onTap: (localMs: Long) -> Unit
) {
    val widthPx = with(LocalDensity.current) { width.toPx() }
    val base = blockColors[colorIndex % blockColors.size]
    val blockShape = RoundedCornerShape(8.dp)
    val borderModifier = when {
        selected -> Modifier.border(2.dp, Amber, blockShape)
        missing -> Modifier.border(1.dp, ErrorRed, blockShape)
        else -> Modifier
    }
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .pointerInput(clip.id, widthPx, clip.durationMs) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / widthPx).coerceIn(0f, 1f)
                    onTap((fraction * clip.durationMs).toLong())
                }
            }
            .padding(horizontal = 1.dp)
            .clip(blockShape)
            .background(Brush.horizontalGradient(listOf(base, base.copy(alpha = 0.7f))))
            .then(borderModifier)
    ) {
        Column(Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
            Text(
                text = clip.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
            Text(
                text = if (missing) "Missing" else formatDuration(clip.durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = if (missing) ErrorRed else TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}
