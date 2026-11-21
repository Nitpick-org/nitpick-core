package dev.nitpick.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme

/**
 * The IDE main toolbar: dark graphite in both themes, 40dp high, 12sp labels. This is the
 * one intentionally chrome-heavy surface; everything below it is calmer.
 */
@Composable
fun NitpickToolbar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NitpickTheme.colors.toolbar,
        contentColor = NitpickTheme.colors.onToolbar,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(NitpickTheme.sizes.header).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = content,
        )
    }
}

@Composable
fun NitpickToolbarAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val colors = NitpickTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier.height(NitpickTheme.sizes.control),
        shape = RoundedCornerShape(NitpickTheme.radii.xs),
        color = if (selected) colors.primary else Color.Transparent,
        contentColor = if (selected) colors.onPrimary else colors.onToolbar,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = NitpickTheme.typography.label,
                color = if (selected) colors.onPrimary else colors.onToolbar,
                maxLines = 1,
            )
        }
    }
}

/** Tool-window header: 28dp, panel-tinted, uppercase 11sp label, actions on the right. */
@Composable
fun NitpickToolWindowHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NitpickTheme.colors.panelHeader,
        contentColor = NitpickTheme.colors.onPanelHeader,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(NitpickTheme.sizes.toolWindowHeader)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title.uppercase(),
                style = NitpickTheme.typography.kicker,
                color = NitpickTheme.colors.onPanelHeader,
            )
            Spacer(Modifier.weight(1f))
            actions()
        }
    }
}

/** IDE-style tab: 28dp content, selected tab uses the panel surface and a 2dp blue rail. */
@Composable
fun NitpickTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NitpickTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier.height(NitpickTheme.sizes.control),
        shape = RoundedCornerShape(topStart = NitpickTheme.radii.xs, topEnd = NitpickTheme.radii.xs),
        color = if (selected) colors.surface else Color.Transparent,
        contentColor = if (selected) colors.text else colors.textSecondary,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = if (selected) NitpickTheme.typography.bodyStrong else NitpickTheme.typography.body,
                color = if (selected) colors.text else colors.textSecondary,
                maxLines = 1,
            )
        }
    }
}

/** IDE status bar: 22dp, muted 11sp text, no shadow. */
@Composable
fun NitpickStatusBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NitpickTheme.colors.statusBar,
        contentColor = NitpickTheme.colors.onStatusBar,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(NitpickTheme.sizes.statusBar).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}
