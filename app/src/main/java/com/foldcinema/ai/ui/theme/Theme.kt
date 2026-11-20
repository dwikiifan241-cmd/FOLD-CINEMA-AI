package com.foldcinema.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CinemaColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = CinemaBlack,
    secondary = Teal,
    onSecondary = CinemaBlack,
    background = CinemaBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TextMuted,
    outline = OutlineDark,
    error = ErrorRed
)

/** The app is dark-only by design (cinematic UI). */
@Composable
fun FoldCinemaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CinemaColorScheme,
        typography = FoldTypography,
        content = content
    )
}
