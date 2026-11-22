package dev.nitpick.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme
import org.jetbrains.compose.resources.DrawableResource

enum class NitpickButtonVariant { Primary, Secondary, Ghost, Danger }

enum class NitpickButtonSize(val height: Dp, val horizontalPadding: Dp) {
    Small(28.dp, 10.dp),
    Medium(32.dp, 12.dp),
    Large(36.dp, 14.dp),
}

/**
 * Dense action button. Primary is the only saturated fill; secondary is a hairline surface;
 * ghost is for toolbar/secondary actions; danger is a soft red surface, never a solid red
 * block (the destructive action stays readable and the icon/text carries the meaning).
 */
@Composable
fun NitpickButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: NitpickButtonVariant = NitpickButtonVariant.Primary,
    size: NitpickButtonSize = NitpickButtonSize.Medium,
    enabled: Boolean = true,
    icon: DrawableResource? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = NitpickTheme.colors
    val (background, foreground, border) = when (variant) {
        NitpickButtonVariant.Primary -> Triple(colors.primary, colors.onPrimary, null)
        NitpickButtonVariant.Secondary -> Triple(colors.surface, colors.text, colors.border)
        NitpickButtonVariant.Ghost -> Triple(colors.surface.copy(alpha = 0f), colors.textSecondary, null)
        NitpickButtonVariant.Danger -> Triple(colors.dangerSoft, colors.danger, colors.danger.copy(alpha = 0.28f))
    }
    val labelStyle = when (size) {
        NitpickButtonSize.Small -> NitpickTheme.typography.small
        NitpickButtonSize.Medium -> NitpickTheme.typography.label
        NitpickButtonSize.Large -> NitpickTheme.typography.bodyStrong
    }

    val pop = rememberIconPop()
    Surface(
        onClick = { pop.play(); onClick() },
        enabled = enabled,
        modifier = modifier.height(size.height).alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(NitpickTheme.radii.md),
        color = background,
        contentColor = foreground,
        border = border?.let { BorderStroke(1.dp, it) },
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = size.horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let { NitpickIcon(it, contentDescription = null, modifier = pop.modifier, tint = foreground) }
            leading?.invoke()
            Text(text = text, style = labelStyle, color = foreground, maxLines = 1)
        }
    }
}
