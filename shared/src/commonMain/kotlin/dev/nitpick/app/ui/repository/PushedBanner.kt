package dev.nitpick.app.ui.repository

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.components.NitpickIcon
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.ic_check
import nitpick.shared.generated.resources.push_done
import org.jetbrains.compose.resources.stringResource

/** « Pushed to origin/branch »: the check pops in, the line fades in. Shown until the next commit or sync. */
@Composable
internal fun PushedBanner(branch: String, modifier: Modifier = Modifier) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val pop by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    )
    val fade by animateFloatAsState(targetValue = if (shown) 1f else 0f, animationSpec = tween(durationMillis = 400))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(fade)
            .background(NitpickTheme.colors.successSoft, RoundedCornerShape(NitpickTheme.radii.md))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NitpickIcon(Res.drawable.ic_check, contentDescription = null, tint = NitpickTheme.colors.success, modifier = Modifier.size(20.dp).scale(pop))
        Text(stringResource(Res.string.push_done, branch), style = NitpickTheme.typography.bodyStrong, color = NitpickTheme.colors.success)
    }
}
