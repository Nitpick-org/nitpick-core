package dev.nitpick.app.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme
import org.jetbrains.compose.resources.DrawableResource

private val RadioSize = 16.dp
private val RadioDotSize = 6.dp

/**
 * Radio group: the active radio is a full `primary` disc with an `onPrimary` dot. Horizontal
 * on desktop; [vertical] with [subtitle] and a taller [rowHeight] for touch screens. With
 * [icon], the mark replaces the label, which stays as content description and tooltip.
 * [trailing] renders after the options, for a [NitpickRadioPlaceholder].
 */
@Composable
fun <T> NitpickRadioGroup(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: ((T) -> DrawableResource)? = null,
    subtitle: ((T) -> String)? = null,
    vertical: Boolean = false,
    rowHeight: Dp = NitpickTheme.sizes.control,
    trailing: (@Composable () -> Unit)? = null,
) {
    if (vertical) {
        Column(modifier = modifier) {
            options.forEach { option -> RadioRow(option, option == selected, label, icon, subtitle, rowHeight, Modifier.fillMaxWidth()) { onSelect(option) } }
            trailing?.invoke()
        }
    } else {
        Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            options.forEach { option -> RadioRow(option, option == selected, label, icon, subtitle, rowHeight, Modifier) { onSelect(option) } }
            trailing?.invoke()
        }
    }
}

/** A greyed entry for an option that cannot be chosen yet: no click, the reason in the tooltip. */
@Composable
fun NitpickRadioPlaceholder(
    label: String,
    icon: DrawableResource,
    hint: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    rowHeight: Dp = NitpickTheme.sizes.control,
) {
    val colors = NitpickTheme.colors
    Row(
        modifier = modifier.height(rowHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioDisc(active = false, enabled = false, indication = Modifier)
        RadioContent(label, icon, subtitle, tooltip = "$label · $hint", iconTint = colors.textMuted, textColor = colors.textMuted)
    }
}

@Composable
private fun <T> RadioRow(
    option: T,
    active: Boolean,
    label: (T) -> String,
    icon: ((T) -> DrawableResource)?,
    subtitle: ((T) -> String)?,
    rowHeight: Dp,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = NitpickTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier.height(rowHeight).clickable(interactionSource = interaction, indication = null, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioDisc(active, enabled = true, indication = Modifier.indication(interaction, LocalIndication.current))
        RadioContent(
            label = label(option),
            icon = icon?.invoke(option),
            subtitle = subtitle?.invoke(option),
            tooltip = label(option),
            iconTint = if (active) colors.text else colors.textSecondary,
            textColor = colors.text,
        )
    }
}

@Composable
private fun RadioDisc(active: Boolean, enabled: Boolean, indication: Modifier) {
    val colors = NitpickTheme.colors
    val ring = when {
        active -> colors.primary
        enabled -> colors.borderStrong
        else -> colors.border
    }
    Box(
        modifier = Modifier
            .size(RadioSize)
            .clip(CircleShape)
            .then(indication)
            .background(if (active) colors.primary else colors.surface)
            .border(1.dp, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (active) Box(Modifier.size(RadioDotSize).clip(CircleShape).background(colors.onPrimary))
    }
}

@Composable
private fun RadioContent(
    label: String,
    icon: DrawableResource?,
    subtitle: String?,
    tooltip: String,
    iconTint: Color,
    textColor: Color,
) {
    Column {
        if (icon == null) {
            Text(label, style = NitpickTheme.typography.body, color = textColor)
        } else {
            NitpickTooltip(tooltip) {
                NitpickIcon(icon, contentDescription = tooltip, tint = iconTint)
            }
        }
        subtitle?.let { Text(it, style = NitpickTheme.typography.micro, color = NitpickTheme.colors.textMuted) }
    }
}
