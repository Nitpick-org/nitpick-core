package dev.nitpick.app.ui.repository

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.FileChangeType
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.ui.components.DiffLineRow
import dev.nitpick.app.ui.components.NitpickBadge
import dev.nitpick.app.ui.components.NitpickEmptyState
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.diff_base
import nitpick.shared.generated.resources.diff_empty
import org.jetbrains.compose.resources.stringResource

/** The Diff inspector tab: the current branch against the configured base. */
@Composable
fun DiffPanel(diff: List<FileDiff>, baseRef: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${stringResource(Res.string.diff_base)}: $baseRef",
                style = NitpickTheme.typography.small,
                color = NitpickTheme.colors.textSecondary,
            )
            Text(
                text = "${diff.size} files",
                style = NitpickTheme.typography.micro,
                color = NitpickTheme.colors.textMuted,
            )
        }
        if (diff.isEmpty()) {
            NitpickEmptyState(
                title = stringResource(Res.string.diff_empty),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(diff, key = { it.path }) { file ->
                    Column(Modifier.fillMaxWidth()) {
                        FileHeader(file)
                        file.hunks.forEach { hunk ->
                            Text(
                                text = "@@ -${hunk.oldStart},${hunk.oldCount} +${hunk.newStart},${hunk.newCount} @@",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                style = NitpickTheme.typography.codeSmall,
                                color = NitpickTheme.colors.textMuted,
                                fontFamily = FontFamily.Monospace,
                            )
                            hunk.lines.forEach { line -> DiffLineRow(line) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileHeader(file: FileDiff) {
    val added = file.hunks.sumOf { hunk -> hunk.lines.count { it.type == DiffLineType.ADDED } }
    val removed = file.hunks.sumOf { hunk -> hunk.lines.count { it.type == DiffLineType.REMOVED } }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = file.path,
            modifier = Modifier.weight(1f),
            style = NitpickTheme.typography.codeSmall,
            color = NitpickTheme.colors.text,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (file.changeType != FileChangeType.MODIFIED) {
            NitpickBadge(file.changeType.name, tone = dev.nitpick.app.ui.theme.NitpickTone.Neutral)
        }
        Text("+$added", style = NitpickTheme.typography.codeSmall, color = NitpickTheme.colors.success)
        Text("-$removed", style = NitpickTheme.typography.codeSmall, color = NitpickTheme.colors.danger)
    }
}
