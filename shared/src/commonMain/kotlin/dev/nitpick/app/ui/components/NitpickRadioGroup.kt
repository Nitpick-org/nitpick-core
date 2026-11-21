package dev.nitpick.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme

private val RadioSize = 16.dp
private val RadioDotSize = 6.dp

/** Horizontal radio group: the active radio is a full `primary` disc with an `onPrimary` dot. */
@Composable
fun <T> NitpickRadioGroup(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NitpickTheme.colors
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        options.forEach { option ->
            val active = option == selected
            Row(
                modifier = Modifier.height(NitpickTheme.sizes.control).clickable { onSelect(option) },
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
                Text(label(option), style = NitpickTheme.typography.body, color = colors.text)
            }
        }
    }
}
