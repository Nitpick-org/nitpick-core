package dev.nitpick.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** A 16 dp JetBrains-style line icon; [contentDescription] is required when the icon is alone. */
@Composable
fun NitpickIcon(
    resource: DrawableResource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        modifier = modifier.size(16.dp),
        colorFilter = if (tint == Color.Unspecified) null else ColorFilter.tint(tint),
    )
}

private const val ICON_POP_SHRINK = 0.5f
private const val ICON_POP_SHRINK_MS = 90

/** A click squeezes the icon, then a spring lets it bounce back: the button answers before the work does. */
class IconPop internal constructor(
    private val scale: Animatable<Float, AnimationVector1D>,
    private val scope: CoroutineScope,
) {
    val modifier: Modifier
        get() = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }

    fun play() {
        scope.launch {
            scale.snapTo(1f)
            scale.animateTo(ICON_POP_SHRINK, tween(ICON_POP_SHRINK_MS))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
        }
    }
}

@Composable
fun rememberIconPop(): IconPop {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    return remember { IconPop(scale, scope) }
}
