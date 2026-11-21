package dev.nitpick.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme
import dev.nitpick.app.ui.theme.NitpickTone
import dev.nitpick.app.ui.theme.tone

/**
 * The standard surface. Cards are defined by a hairline border and a slightly raised tone,
 * not by a drop shadow; shadows are reserved for floating/popover layers.
 */
@Composable
fun NitpickCard(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(NitpickTheme.radii.lg),
        color = NitpickTheme.colors.surface,
        contentColor = NitpickTheme.colors.text,
        border = BorderStroke(1.dp, NitpickTheme.colors.border),
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(padding), verticalArrangement = verticalArrangement, content = content)
    }
}

@Composable
fun NitpickBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: NitpickTone = NitpickTone.Neutral,
) {
    val colors = NitpickTheme.colors.tone(tone)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(NitpickTheme.radii.pill),
        color = colors.background,
        contentColor = colors.foreground,
        border = BorderStroke(1.dp, colors.border),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = text, style = NitpickTheme.typography.micro, color = colors.foreground, maxLines = 1)
        }
    }
}

/** A keyboard shortcut hint; monospace so it aligns like code. */
@Composable
fun NitpickKbd(key: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(NitpickTheme.radii.xs),
        color = NitpickTheme.colors.surfaceSunken,
        contentColor = NitpickTheme.colors.textSecondary,
        border = BorderStroke(1.dp, NitpickTheme.colors.border),
        tonalElevation = 0.dp,
    ) {
        Text(
            text = key,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            style = NitpickTheme.typography.codeSmall,
            color = NitpickTheme.colors.textSecondary,
        )
    }
}

@Composable
fun NitpickDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.fillMaxWidth().height(1.dp), color = NitpickTheme.colors.border)
}

@Composable
fun NitpickVerticalDivider(modifier: Modifier = Modifier) {
    VerticalDivider(modifier = modifier.fillMaxHeight(), color = NitpickTheme.colors.border)
}
