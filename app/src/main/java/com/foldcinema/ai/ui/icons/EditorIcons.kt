package com.foldcinema.ai.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Small vector icons built from plain polygons (the core icon set has no pause/volume/split). */
object EditorIcons {

    val Pause: ImageVector by lazy {
        build("Pause") {
            poly(6f, 5f, 10f, 5f, 10f, 19f, 6f, 19f)
            poly(14f, 5f, 18f, 5f, 18f, 19f, 14f, 19f)
        }
    }

    val Volume: ImageVector by lazy {
        build("Volume") {
            poly(3f, 9f, 7f, 9f, 12f, 4f, 12f, 20f, 7f, 15f, 3f, 15f)
            poly(15f, 9f, 17f, 9f, 17f, 15f, 15f, 15f)
            poly(19f, 6f, 21f, 6f, 21f, 18f, 19f, 18f)
        }
    }

    val Split: ImageVector by lazy {
        build("Split") {
            poly(11f, 3f, 13f, 3f, 13f, 21f, 11f, 21f)
            poly(4f, 8f, 9f, 12f, 4f, 16f)
            poly(20f, 8f, 15f, 12f, 20f, 16f)
        }
    }

    val Trim: ImageVector by lazy {
        build("Trim") {
            poly(4f, 4f, 10f, 4f, 10f, 6f, 6f, 6f, 6f, 18f, 10f, 18f, 10f, 20f, 4f, 20f)
            poly(20f, 4f, 14f, 4f, 14f, 6f, 18f, 6f, 18f, 18f, 14f, 18f, 14f, 20f, 20f, 20f)
        }
    }

    val MoveLeft: ImageVector by lazy {
        build("MoveLeft") { poly(15f, 5f, 7f, 12f, 15f, 19f) }
    }

    val MoveRight: ImageVector by lazy {
        build("MoveRight") { poly(9f, 5f, 17f, 12f, 9f, 19f) }
    }

    private fun build(name: String, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black), pathBuilder = block).build()

    private fun PathBuilder.poly(vararg p: Float) {
        moveTo(p[0], p[1])
        var i = 2
        while (i + 1 < p.size) {
            lineTo(p[i], p[i + 1])
            i += 2
        }
        close()
    }
}
