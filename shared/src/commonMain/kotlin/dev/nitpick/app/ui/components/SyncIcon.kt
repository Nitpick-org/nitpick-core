package dev.nitpick.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource

/** How a sync icon moves while its operation runs: the fetch arrows spin, the pull arrow drops, the push arrow rises. */
enum class SyncMotion { Spin, Down, Up }

private const val SPIN_MILLIS = 900
private const val BOB_MILLIS = 450
private val BobDistance = 3.dp

/** A 16 dp icon that moves only while [active]; still otherwise, so idle buttons cost no frames. */
@Composable
fun SyncIcon(resource: DrawableResource, motion: SyncMotion, active: Boolean, tint: Color, modifier: Modifier = Modifier) {
    if (!active) {
        NitpickIcon(resource, contentDescription = null, tint = tint, modifier = modifier)
        return
    }
    val transition = rememberInfiniteTransition(label = "sync")
    val bob = with(LocalDensity.current) { BobDistance.toPx() }
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = when (motion) {
            SyncMotion.Spin -> infiniteRepeatable(tween(SPIN_MILLIS, easing = LinearEasing))
            SyncMotion.Down, SyncMotion.Up -> infiniteRepeatable(tween(BOB_MILLIS), RepeatMode.Reverse)
        },
        label = "progress",
    )
    NitpickIcon(
        resource,
        contentDescription = null,
        tint = tint,
        modifier = modifier.graphicsLayer {
            when (motion) {
                SyncMotion.Spin -> rotationZ = progress * 360f
                SyncMotion.Down -> translationY = progress * bob
                SyncMotion.Up -> translationY = -progress * bob
            }
        },
    )
}
