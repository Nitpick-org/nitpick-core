package dev.nitpick.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme

/** IDE-style segmented control: sunken container, the active segment raised on `surface`. */
@Composable
fun <T> NitpickSegmentedControl(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = NitpickTheme.sizes.control,
) {
    val colors = NitpickTheme.colors
    val container = RoundedCornerShape(NitpickTheme.radii.md)
    val segment = RoundedCornerShape(NitpickTheme.radii.sm)
    Row(
        modifier = modifier
            .height(height)
            .background(colors.surfaceSunken, container)
            .border(1.dp, colors.border, container)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { option ->
            val active = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(segment)
                    .background(if (active) colors.surface else Color.Transparent)
                    .then(if (active) Modifier.border(1.dp, colors.borderStrong, segment) else Modifier)
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(option),
                    style = if (active) NitpickTheme.typography.bodyStrong else NitpickTheme.typography.body,
                    color = if (active) colors.text else colors.textSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}
