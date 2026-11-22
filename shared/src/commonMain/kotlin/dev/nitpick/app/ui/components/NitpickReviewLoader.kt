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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme
import kotlinx.coroutines.delay
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.review_phrases
import org.jetbrains.compose.resources.stringArrayResource
import kotlin.math.PI
import kotlin.math.sin

// The tweezers of ic_tweezers.xml, in its 24 dp viewport: a bar and two arms meeting at the tips.
private val TweezerLines = listOf(
    listOf(Offset(10.4f, 2.5f), Offset(13.6f, 2.5f)),
    listOf(Offset(10.4f, 2.5f), Offset(6.8f, 14.2f), Offset(10.4f, 19.2f)),
    listOf(Offset(13.6f, 2.5f), Offset(17.2f, 14.2f), Offset(13.6f, 19.2f)),
)
private const val VIEWPORT = 24f
private const val STROKE = 1.8f
private val Center = Offset(12f, 11f)
private val Dot = Offset(12f, 21.6f)
private const val CYCLE_MS = 4200
private const val PHRASE_MS = 2400L

/** The tweezers symbol drawing itself, pinching, and dissolving on a loop, with a phrase that changes. */
@Composable
fun NitpickReviewLoader(modifier: Modifier = Modifier, symbolSize: Dp = 72.dp) {
    val phrases = stringArrayResource(Res.array.review_phrases)
    val order = remember(phrases) { phrases.shuffled() }
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(order) {
        while (true) {
            delay(PHRASE_MS)
            index = (index + 1) % order.size
        }
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TweezersSymbol(Modifier.size(symbolSize), NitpickTheme.colors.primary, NitpickTheme.colors.text)
        Crossfade(order[index]) { phrase ->
            Text(phrase, style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
        }
    }
}

@Composable
private fun TweezersSymbol(modifier: Modifier, stroke: Color, dot: Color) {
    val transition = rememberInfiniteTransition()
    val t by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(CYCLE_MS, easing = LinearEasing)))
    Canvas(modifier) {
        val unit = size.minDimension / VIEWPORT
        val pose = poseAt(t)
        drawHalo(stroke, unit, breath = 0.5f + 0.5f * sin(t * 2f * PI).toFloat(), fade = 1f - pose.collapse)
        rotate(degrees = pose.sway, pivot = Center * unit) {
            drawLines(TweezerLines, pose, unit, stroke)
        }
        if (pose.ripple > 0f) {
            drawCircle(stroke.copy(alpha = 0.45f * (1f - pose.ripple)), radius = (1.5f + 8f * pose.ripple) * unit, center = Dot * unit, style = Stroke(0.5f * unit))
        }
        val pulse = 1f + 0.3f * sin(t * 4f * PI).toFloat()
        drawCircle(stroke.copy(alpha = 0.35f), radius = 2.6f * unit * pulse, center = Dot * unit)
        drawCircle(dot, radius = 1.3f * unit * pulse, center = Dot * unit)
    }
}

/** One frame of the loop: how much is drawn, how far the tips pinch, the sway angle, the collapse, the ripple. */
private class Pose(val drawn: Float, val pinch: Float, val sway: Float, val collapse: Float, val ripple: Float)

private fun poseAt(t: Float): Pose = when {
    // Draws itself, a bright tip leading the stroke.
    t < 0.30f -> Pose(drawn = easeOut(t / 0.30f), pinch = 0f, sway = 0f, collapse = 0f, ripple = 0f)
    // Alive: pinches twice like picking a nit, and sways a little.
    t < 0.72f -> {
        val u = (t - 0.30f) / 0.42f
        Pose(drawn = 1f, pinch = sin(u * 2f * PI).toFloat().coerceAtLeast(0f), sway = 4f * sin(u * 3f * PI).toFloat(), collapse = 0f, ripple = 0f)
    }
    // Melts into the dot, which answers with a ripple.
    t < 0.86f -> {
        val u = (t - 0.72f) / 0.14f
        Pose(drawn = 1f, pinch = 0f, sway = 0f, collapse = easeOut(u), ripple = u)
    }
    // The dot waits alone before the next drawing.
    else -> Pose(drawn = 0f, pinch = 0f, sway = 0f, collapse = 1f, ripple = 0f)
}

private fun easeOut(x: Float): Float = 1f - (1f - x) * (1f - x)

private fun DrawScope.drawHalo(color: Color, unit: Float, breath: Float, fade: Float) {
    val radius = (9f + 2f * breath) * unit
    drawCircle(
        brush = Brush.radialGradient(listOf(color.copy(alpha = 0.16f * fade), color.copy(alpha = 0f)), center = Center * unit, radius = radius),
        radius = radius,
        center = Center * unit,
    )
}

private fun DrawScope.drawLines(lines: List<List<Offset>>, pose: Pose, unit: Float, color: Color) {
    if (pose.drawn <= 0f || pose.collapse >= 1f) return
    val total = lines.sumOf { it.length().toDouble() }.toFloat()
    var budget = total * pose.drawn
    val alpha = 1f - pose.collapse
    val width = STROKE * unit * (1f + 0.15f * pose.pinch)
    var tip: Offset? = null
    for (line in lines) {
        var from = line.first()
        for (to in line.drop(1)) {
            if (budget <= 0f) break
            val length = (to - from).getDistance()
            val end = if (length <= budget) to else from + (to - from) * (budget / length)
            budget -= length
            drawLine(color.copy(alpha = alpha), deform(from, pose) * unit, deform(end, pose) * unit, strokeWidth = width, cap = StrokeCap.Round)
            if (budget < 0f) tip = end
            from = to
        }
    }
    // The pen tip glows while the outline is still being drawn.
    if (pose.drawn < 1f) tip?.let { p ->
        drawCircle(color.copy(alpha = 0.35f), radius = 2.4f * unit, center = deform(p, pose) * unit)
        drawCircle(Color.White.copy(alpha = 0.9f), radius = 1.1f * unit, center = deform(p, pose) * unit)
    }
}

/** The arms bend toward the centre line as the tips pinch; the whole shape sinks into the dot as it collapses. */
private fun deform(p: Offset, pose: Pose): Offset {
    val depth = ((p.y - 2.5f) / (19.2f - 2.5f)).coerceIn(0f, 1f)
    val pinched = Offset(p.x + (Center.x - p.x) * 0.55f * pose.pinch * depth * depth, p.y)
    return pinched + (Dot - pinched) * pose.collapse
}

private fun List<Offset>.length(): Float = zipWithNext { a, b -> (b - a).getDistance() }.sum()
