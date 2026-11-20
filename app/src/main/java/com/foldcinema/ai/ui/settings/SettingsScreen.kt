package com.foldcinema.ai.ui.settings

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.foldcinema.ai.ui.adaptive.WidthClass
import com.foldcinema.ai.ui.components.CenteredScrollColumn
import com.foldcinema.ai.ui.components.CinemaBackground
import com.foldcinema.ai.ui.components.CinemaTopBar
import com.foldcinema.ai.ui.components.InfoCard
import com.foldcinema.ai.ui.components.InfoRow

@Composable
fun SettingsScreen(widthClass: WidthClass, onBack: () -> Unit) {
    val context = LocalContext.current
    val config = LocalConfiguration.current

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "-"
        } catch (e: Exception) {
            "-"
        }
    }
    val glEsVersion = remember {
        try {
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo.glEsVersion
        } catch (e: Exception) {
            "-"
        }
    }

    CinemaBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            CinemaTopBar(title = "Settings", onBack = onBack)
            CenteredScrollColumn {
                InfoCard(title = "Display") {
                    InfoRow("Layout", widthClass.label)
                    InfoRow("Window size", "${config.screenWidthDp} × ${config.screenHeightDp} dp")
                    InfoRow("Orientation", if (config.screenWidthDp > config.screenHeightDp) "Landscape" else "Portrait")
                }
                InfoCard(title = "Device") {
                    InfoRow("Model", "${Build.MANUFACTURER} ${Build.MODEL}")
                    InfoRow("Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    InfoRow("OpenGL ES", glEsVersion)
                }
                InfoCard(title = "Application") {
                    InfoRow("Version", versionName)
                    InfoRow("Stage", "2 · Video editor core")
                }
                InfoCard(title = "Engine roadmap") {
                    InfoRow("Playback (Media3 ExoPlayer)", "Active")
                    InfoRow("Multi-clip timeline & editing", "Active")
                    InfoRow("Export (MediaCodec/MediaMuxer)", "Planned")
                    InfoRow("Color grading (OpenGL ES 3.0)", "Planned")
                    InfoRow("AI Auto Edit", "Planned")
                }
            }
        }
    }
}
