package dev.nitpick.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme

/**
 * Page/panel heading. The kicker is optional and always uppercase; the title carries the
 * hierarchy, actions sit on the same baseline as the title on desktop.
 */
@Composable
fun NitpickSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    kicker: String? = null,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (kicker != null) {
            Text(
                text = kicker.uppercase(),
                style = NitpickTheme.typography.kicker,
                color = NitpickTheme.colors.textMuted,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = NitpickTheme.typography.section,
                color = NitpickTheme.colors.text,
            )
            Spacer(Modifier.weight(1f))
            actions()
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = NitpickTheme.typography.small,
                color = NitpickTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
fun NitpickEmptyState(
    title: String,
    detail: String? = null,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = NitpickTheme.typography.title,
            color = NitpickTheme.colors.text,
            textAlign = TextAlign.Center,
        )
        if (detail != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = detail,
                style = NitpickTheme.typography.small,
                color = NitpickTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            NitpickButton(text = actionLabel, onClick = onAction)
        }
    }
}
