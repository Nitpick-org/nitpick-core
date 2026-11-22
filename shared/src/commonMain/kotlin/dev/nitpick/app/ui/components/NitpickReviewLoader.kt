package dev.nitpick.app.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme
import kotlinx.coroutines.delay
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.review_phrases
import org.jetbrains.compose.resources.stringArrayResource

// Geometry and keyframes of docs/design/loader/nitpick-symbol-loader.svg (viewBox 86 56 340 352).
private const val VIEW_X = 86f
private const val VIEW_Y = 56f
private const val VIEW_W = 340f
private const val VIEW_H = 352f
private const val STROKE = 24f
private val BarLeft = Offset(234.4f, 121f)
private val BarRight = Offset(277.6f, 121f)
private val LeftArm = listOf(BarLeft, Offset(185.8f, 279f), Offset(234.4f, 346.5f))
private val RightArm = listOf(BarRight, Offset(326.2f, 279f), Offset(277.6f, 346.5f))
private val DotCenter = Offset(256f, 371f)
private const val DOT_RADIUS = 20f
private val ShadowCenter = Offset(256f, 398f)
private val ShadowRadii = Size(24f, 5f)
private const val LIFT = 48f
private const val OPEN_DEGREES = 5f
private const val CYCLE_MS = 2400
private const val PHRASE_MS = 2400L

/** The tweezers picking the nit up, lifting it and dropping it, on a loop, with a phrase that changes. */
@Composable
fun NitpickReviewLoader(modifier: Modifier = Modifier, symbolSize: Dp = 72.dp) {
    val phrases = stringArrayResource(Res.array.review_phrases)
    // Keyed on the size, not the list: the resource lookup hands out a new list on every frame of the animation.
    val order = remember(phrases.size) { phrases.shuffled() }
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(order) {
        while (true) {
            delay(PHRASE_MS)
            index = (index + 1) % order.size
        }
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NitpickTweezersSpinner(symbolSize)
        Crossfade(order[index]) { phrase ->
            Text(phrase, style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
        }
    }
}

/** The 16 dp spinner next to a short status, for toolbars and status lines (loader preview, "inline"). */
@Composable
fun NitpickInlineLoader(text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NitpickTweezersSpinner(16.dp)
        Text(text, style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
    }
}

/** The animated tweezers alone, in the accent colour; 120, 48, 24 and 16 dp are the sizes of the loader preview. */
@Composable
fun NitpickTweezersSpinner(size: Dp, modifier: Modifier = Modifier) {
    TweezersSymbol(modifier.size(size), NitpickTheme.colors.primary)
}

@Composable
private fun TweezersSymbol(modifier: Modifier, color: Color) {
    val transition = rememberInfiniteTransition()
    val t by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(CYCLE_MS, easing = LinearEasing)))
    Canvas(modifier) {
        val unit = minOf(size.width / VIEW_W, size.height / VIEW_H)
        val frame = frameAt(t)
        scale(unit, unit, pivot = Offset.Zero) {
            translate(-VIEW_X + (size.width / unit - VIEW_W) / 2f, -VIEW_Y + (size.height / unit - VIEW_H) / 2f) {
                drawShadow(color, frame)
                translate(top = -LIFT * frame.lift) {
                    drawLine(color, BarLeft, BarRight, STROKE, StrokeCap.Round)
                    rotate(OPEN_DEGREES * frame.open, pivot = BarLeft) { drawArm(LeftArm, color) }
                    rotate(-OPEN_DEGREES * frame.open, pivot = BarRight) { drawArm(RightArm, color) }
                }
                translate(top = -LIFT * frame.dotLift) {
                    scale(frame.squashX, frame.squashY, pivot = DotCenter) {
                        drawCircle(color, DOT_RADIUS, DotCenter)
                    }
                }
            }
        }
    }
}

/** One frame of the SVG keyframes: arms open (1) or closed (0), lift of the tweezers and of the dot, dot squash, shadow. */
private class Frame(
    val open: Float,
    val lift: Float,
    val dotLift: Float,
    val squashX: Float,
    val squashY: Float,
    val shadowScale: Float,
    val shadowAlpha: Float,
)

private fun frameAt(t: Float): Frame = Frame(
    open = keyframes(t, 0f to 1f, 0.06f to 1f, 0.18f to 0f, 0.62f to 0f, 0.70f to 1f, 1f to 1f),
    lift = keyframes(t, 0f to 0f, 0.20f to 0f, 0.40f to 1f, 0.64f to 1f, 0.90f to 0f, 1f to 0f),
    dotLift = keyframes(t, 0f to 0f, 0.20f to 0f, 0.40f to 1f, 0.66f to 1f, 0.79f to 0f, 0.85f to 0.25f, 0.91f to 0f, 1f to 0f),
    squashX = keyframes(t, 0f to 1f, 0.66f to 1f, 0.79f to 1.2f, 0.85f to 0.94f, 0.91f to 1.06f, 0.96f to 1f, 1f to 1f),
    squashY = keyframes(t, 0f to 1f, 0.66f to 1f, 0.79f to 0.78f, 0.85f to 1.06f, 0.91f to 0.94f, 0.96f to 1f, 1f to 1f),
    shadowScale = keyframes(t, 0f to 1f, 0.20f to 1f, 0.40f to 0.55f, 0.66f to 0.55f, 0.79f to 1.15f, 0.85f to 0.85f, 0.91f to 1f, 1f to 1f),
    shadowAlpha = keyframes(t, 0f to 0.22f, 0.20f to 0.22f, 0.40f to 0.08f, 0.66f to 0.08f, 0.79f to 0.26f, 0.85f to 0.14f, 0.91f to 0.22f, 1f to 0.22f),
)

/** Piecewise interpolation between (time, value) stops with the SVG's ease-in-out between each pair. */
private fun keyframes(t: Float, vararg stops: Pair<Float, Float>): Float {
    for (i in 1 until stops.size) {
        val (t0, v0) = stops[i - 1]
        val (t1, v1) = stops[i]
        if (t <= t1) {
            if (t1 == t0) return v1
            val u = ((t - t0) / (t1 - t0)).coerceIn(0f, 1f)
            return v0 + (v1 - v0) * easeInOut(u)
        }
    }
    return stops.last().second
}

private fun easeInOut(u: Float): Float = if (u < 0.5f) 2f * u * u else 1f - 2f * (1f - u) * (1f - u)

private fun DrawScope.drawArm(points: List<Offset>, color: Color) {
    points.zipWithNext { a, b -> drawLine(color, a, b, STROKE, StrokeCap.Round) }
}

private fun DrawScope.drawShadow(color: Color, frame: Frame) {
    val radii = ShadowRadii * frame.shadowScale
    drawOval(
        color.copy(alpha = frame.shadowAlpha),
        topLeft = Offset(ShadowCenter.x - radii.width, ShadowCenter.y - radii.height),
        size = Size(radii.width * 2f, radii.height * 2f),
    )
}
