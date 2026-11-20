package com.foldcinema.ai.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Width buckets following Material window size class breakpoints (600dp / 840dp).
 * Galaxy Z Fold6 cover screen is roughly 360dp wide (Compact); the unfolded
 * inner display is roughly 670dp wide in portrait (Medium) and wider in landscape (Expanded).
 */
enum class WidthClass(val label: String) {
    Compact("Cover screen"),
    Medium("Unfolded"),
    Expanded("Unfolded · wide")
}

@Composable
fun rememberWidthClass(): WidthClass {
    // LocalConfiguration is updated on fold/unfold because MainActivity handles configChanges.
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp < 600 -> WidthClass.Compact
        widthDp < 840 -> WidthClass.Medium
        else -> WidthClass.Expanded
    }
}
