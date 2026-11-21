package dev.nitpick.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme

/** Hover tooltip for toolbar actions: raised surface, 10dp radius, optional shortcut as a kbd. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NitpickTooltip(
    text: String,
    modifier: Modifier = Modifier,
    shortcut: String? = null,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        tooltip = {
            Surface(
                shape = RoundedCornerShape(NitpickTheme.radii.xl),
                color = NitpickTheme.colors.surfaceRaised,
                contentColor = NitpickTheme.colors.text,
                border = BorderStroke(1.dp, NitpickTheme.colors.border),
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text, style = NitpickTheme.typography.small, color = NitpickTheme.colors.text)
                    if (shortcut != null) NitpickKbd(shortcut)
                }
            }
        },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content,
    )
}
