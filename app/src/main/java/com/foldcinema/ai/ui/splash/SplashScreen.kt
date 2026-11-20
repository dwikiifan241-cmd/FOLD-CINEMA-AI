package com.foldcinema.ai.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foldcinema.ai.R
import com.foldcinema.ai.ui.components.BrandWordmark
import com.foldcinema.ai.ui.components.CinemaBackground
import com.foldcinema.ai.ui.theme.TextMuted
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "splashAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.85f,
        animationSpec = tween(durationMillis = 900),
        label = "splashScale"
    )

    LaunchedEffect(Unit) {
        started = true
        delay(2000)
        onFinished()
    }

    CinemaBackground {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "FOLD CINEMA AI logo",
                modifier = Modifier.size(160.dp)
            )
            Spacer(Modifier.height(8.dp))
            BrandWordmark(fontSize = 26.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Professional video editing for Galaxy Z Fold6",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
