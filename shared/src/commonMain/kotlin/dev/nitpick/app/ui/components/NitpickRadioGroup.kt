package dev.nitpick.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme
import org.jetbrains.compose.resources.DrawableResource

private val RadioSize = 16.dp
private val RadioDotSize = 6.dp

/**
 * Radio group: the active radio is a full `primary` disc with an `onPrimary` dot. Horizontal
 * on desktop; [vertical] with [subtitle] and a taller [rowHeight] for touch screens.
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
) {
    if (vertical) {
        Column(modifier = modifier) {
            options.forEach { option -> RadioRow(option, option == selected, label, icon, subtitle, rowHeight, Modifier.fillMaxWidth()) { onSelect(option) } }
        }
    } else {
        Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            options.forEach { option -> RadioRow(option, option == selected, label, icon, subtitle, rowHeight, Modifier) { onSelect(option) } }
        }
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
    Row(
        modifier = modifier.height(rowHeight).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(RadioSize)
                .clip(CircleShape)
                .background(if (active) colors.primary else colors.surface)
                .border(1.dp, if (active) colors.primary else colors.borderStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (active) Box(Modifier.size(RadioDotSize).clip(CircleShape).background(colors.onPrimary))
        }
        Column {
            if (icon == null) {
                Text(label(option), style = NitpickTheme.typography.body, color = colors.text)
            } else {
                NitpickTooltip(label(option)) {
                    NitpickIcon(icon(option), contentDescription = label(option), tint = if (active) colors.text else colors.textSecondary)
                }
            }
            subtitle?.let { Text(it(option), style = NitpickTheme.typography.micro, color = colors.textMuted) }
        }
    }
}
