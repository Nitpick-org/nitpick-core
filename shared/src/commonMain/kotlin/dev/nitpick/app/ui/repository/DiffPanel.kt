package dev.nitpick.app.ui.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.Hunk
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.ui.components.DiffLineRow
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickButtonVariant
import dev.nitpick.app.ui.components.NitpickDivider
import dev.nitpick.app.ui.components.NitpickEmptyState
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val FileListMaxHeight = 168.dp

/** Diff tab of the inspector (docs/design/UI-HANDOFF.md §3.3): the current branch against a base. */
@Composable
fun DiffPanel(
    state: RepositoryUiState,
    onSelectBase: (String) -> Unit,
    onSelectFile: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        DiffHeader(state, onSelectBase)
        NitpickDivider()
        val selected = state.selectedDiff
        when {
            state.isDiffLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NitpickTheme.colors.primary)
            }
            state.diffError != null -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(Res.string.diff_error_title, state.baseRef),
                    style = NitpickTheme.typography.title,
                    color = NitpickTheme.colors.danger,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = state.diffError,
                    modifier = Modifier.padding(top = 8.dp),
                    style = NitpickTheme.typography.small,
                    color = NitpickTheme.colors.danger,
                    textAlign = TextAlign.Center,
                )
            }
            selected == null -> NitpickEmptyState(title = stringResource(Res.string.diff_empty, state.baseRef))
            else -> {
                ChangedFiles(state.diff, selected.path, onSelectFile)
                NitpickDivider()
                FileHunks(selected, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DiffHeader(state: RepositoryUiState, onSelectBase: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(NitpickTheme.sizes.header).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.diff_vs, branchLabel(state.currentBranch)),
            style = NitpickTheme.typography.codeSmall,
            color = NitpickTheme.colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val bases = state.refs
            .filter { it.type == RefType.LOCAL_BRANCH && it.name != state.currentBranch }
            .map { it.name }
        BaseSelector(state.baseRef, bases, onSelectBase)
        Box(Modifier.weight(1f))
        if (state.diff.isNotEmpty() && !state.isDiffLoading) {
            Text(
                text = stringResource(Res.string.diff_files, state.diff.size),
                style = NitpickTheme.typography.small,
                color = NitpickTheme.colors.textSecondary,
            )
            LineCounts(state.diff.sumOf { it.count(DiffLineType.ADDED) }, state.diff.sumOf { it.count(DiffLineType.REMOVED) })
        }
    }
}

@Composable
private fun BaseSelector(baseRef: String, bases: List<String>, onSelectBase: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        NitpickButton(
            text = stringResource(Res.string.diff_base, baseRef) + " ▾",
            onClick = { expanded = true },
            variant = NitpickButtonVariant.Secondary,
            size = NitpickButtonSize.Small,
            enabled = bases.isNotEmpty(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            bases.forEach { base ->
                DropdownMenuItem(
                    text = { Text(base, style = NitpickTheme.typography.codeSmall) },
                    onClick = {
                        expanded = false
                        onSelectBase(base)
                    },
                )
            }
        }
    }
}

@Composable
private fun ChangedFiles(files: List<FileDiff>, selectedPath: String, onSelectFile: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxWidth().heightIn(max = FileListMaxHeight)) {
        items(files, key = { it.path }) { file ->
            val isSelected = file.path == selectedPath
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NitpickTheme.sizes.row)
                    .background(if (isSelected) NitpickTheme.colors.selection else Color.Transparent)
                    .clickable { onSelectFile(file.path) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = file.path,
                    modifier = Modifier.weight(1f),
                    style = NitpickTheme.typography.codeSmall,
                    color = if (isSelected) NitpickTheme.colors.onSelection else NitpickTheme.colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                LineCounts(file.count(DiffLineType.ADDED), file.count(DiffLineType.REMOVED))
            }
        }
    }
}

@Composable
private fun FileHunks(file: FileDiff, modifier: Modifier) {
    LazyColumn(modifier.fillMaxWidth()) {
        item {
            Text(
                text = file.oldPath?.let { "$it → ${file.path}" } ?: file.path,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NitpickTheme.sizes.toolWindowHeader)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = NitpickTheme.typography.codeSmall,
                color = NitpickTheme.colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (file.isBinary || file.hunks.isEmpty()) {
            item {
                Text(
                    text = stringResource(if (file.isBinary) Res.string.diff_binary else Res.string.diff_no_line_changes),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = NitpickTheme.typography.small,
                    color = NitpickTheme.colors.textMuted,
                )
            }
        }
        file.hunks.forEach { hunk ->
            item { HunkHeader(hunk) }
            items(hunk.lines) { line -> DiffLineRow(line) }
        }
    }
}

@Composable
private fun HunkHeader(hunk: Hunk) {
    Text(
        text = "@@ -${hunk.oldStart},${hunk.oldCount} +${hunk.newStart},${hunk.newCount} @@",
        modifier = Modifier
            .fillMaxWidth()
            .background(NitpickTheme.colors.surfaceSunken)
            .padding(horizontal = 12.dp, vertical = 2.dp),
        style = NitpickTheme.typography.codeSmall,
        color = NitpickTheme.colors.textMuted,
    )
}

@Composable
private fun LineCounts(added: Int, removed: Int) {
    Text("+$added", style = NitpickTheme.typography.codeSmall, color = NitpickTheme.colors.success)
    Text("−$removed", style = NitpickTheme.typography.codeSmall, color = NitpickTheme.colors.danger)
}

private fun FileDiff.count(type: DiffLineType): Int = hunks.sumOf { hunk -> hunk.lines.count { it.type == type } }
