package dev.nitpick.app.ui.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.ui.components.NitpickEmptyState
import dev.nitpick.app.ui.components.NitpickStatusBar
import dev.nitpick.app.ui.components.NitpickToolbar
import dev.nitpick.app.ui.components.NitpickToolbarAction
import dev.nitpick.app.ui.components.NitpickToolWindowHeader
import dev.nitpick.app.ui.components.NitpickVerticalDivider
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Desktop shell from docs/CONTEXT.md §12: refs tool window | commit graph | details tool
 * window, framed by the IDE toolbar and status bar. The diff and review land in the details
 * panel in later tickets.
 *
 * [onOpenRepository] is null on platforms without a folder picker.
 */
@Composable
fun RepositoryScreen(
    state: RepositoryUiState,
    onOpenRepository: (() -> Unit)?,
    onSelectCommit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        RepositoryToolbar(state.path, state.currentBranch, onOpenRepository)
        Box(Modifier.weight(1f)) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NitpickTheme.colors.primary)
                }
                state.errorMessage != null -> NitpickEmptyState(
                    title = stringResource(Res.string.repository_error_title),
                    detail = state.errorMessage,
                    actionLabel = if (onOpenRepository != null) stringResource(Res.string.repository_open) else null,
                    onAction = onOpenRepository,
                )
                state.path == null -> NitpickEmptyState(
                    title = stringResource(Res.string.repository_empty_title),
                    detail = if (onOpenRepository != null) stringResource(Res.string.repository_empty_hint) else null,
                    actionLabel = if (onOpenRepository != null) stringResource(Res.string.repository_open) else null,
                    onAction = onOpenRepository,
                )
                else -> Row(Modifier.fillMaxSize()) {
                    RefsSidebar(
                        refs = state.refs,
                        currentBranch = state.currentBranch,
                        modifier = Modifier.width(NitpickTheme.sizes.sidebar).fillMaxHeight(),
                    )
                    NitpickVerticalDivider()
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        NitpickToolWindowHeader(stringResource(Res.string.commits_title))
                        if (state.graph.isEmpty()) {
                            PanelHint(stringResource(Res.string.commits_empty))
                        } else {
                            CommitGraphList(
                                graph = state.graph,
                                selectedCommitId = state.selectedCommitId,
                                onSelectCommit = onSelectCommit,
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                            )
                        }
                    }
                    NitpickVerticalDivider()
                    val selected = state.selectedCommit
                    Column(Modifier.width(NitpickTheme.sizes.inspector).fillMaxHeight()) {
                        NitpickToolWindowHeader(stringResource(Res.string.details_title))
                        if (selected != null) {
                            CommitDetails(selected, Modifier.weight(1f))
                        } else {
                            DetailsPlaceholder(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        NitpickStatusBar {
            Text(
                text = state.currentBranch.ifEmpty { stringResource(Res.string.status_no_branch) },
                style = NitpickTheme.typography.micro,
                color = NitpickTheme.colors.onStatusBar,
            )
            Text(
                text = stringResource(Res.string.status_commits, state.graph.size),
                style = NitpickTheme.typography.micro,
                color = NitpickTheme.colors.onStatusBar,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(Res.string.status_git),
                style = NitpickTheme.typography.micro,
                color = NitpickTheme.colors.onStatusBar,
            )
        }
    }
}

@Composable
private fun RepositoryToolbar(path: String?, currentBranch: String, onOpenRepository: (() -> Unit)?) {
    NitpickToolbar {
        Text(
            text = "Nitpick",
            style = NitpickTheme.typography.bodyStrong,
            color = NitpickTheme.colors.onToolbar,
        )
        if (path != null) {
            Text(
                text = "/ ${folderName(path)}",
                style = NitpickTheme.typography.body,
                color = NitpickTheme.colors.onToolbar.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (currentBranch.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.repository_on_branch, branchLabel(currentBranch)),
                    style = NitpickTheme.typography.small,
                    color = NitpickTheme.colors.onToolbar.copy(alpha = 0.55f),
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (onOpenRepository != null) {
            NitpickToolbarAction(
                text = stringResource(Res.string.repository_open),
                onClick = onOpenRepository,
            )
        }
    }
}

@Composable
private fun RefsSidebar(refs: List<Ref>, currentBranch: String, modifier: Modifier) {
    val sections: List<Pair<StringResource, List<Ref>>> = listOf(
        Res.string.refs_local_branches to refs.filter { it.type == RefType.LOCAL_BRANCH },
        Res.string.refs_remote_branches to refs.filter { it.type == RefType.REMOTE_BRANCH },
        Res.string.refs_tags to refs.filter { it.type == RefType.TAG },
    ).filter { (_, refsInSection) -> refsInSection.isNotEmpty() }

    Column(modifier.background(NitpickTheme.colors.surface)) {
        NitpickToolWindowHeader(stringResource(Res.string.refs_local_branches))
        LazyColumn(Modifier.weight(1f).padding(vertical = 4.dp)) {
            if (sections.isEmpty()) {
                item { PanelHint(stringResource(Res.string.refs_empty)) }
            }
            sections.forEach { (title, refsInSection) ->
                item { RefSectionTitle(stringResource(title)) }
                items(refsInSection, key = { it.fullName }) { ref ->
                    val isCurrent = ref.type == RefType.LOCAL_BRANCH && ref.name == currentBranch
                    Text(
                        text = ref.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isCurrent) NitpickTheme.colors.selection else androidx.compose.ui.graphics.Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 3.dp),
                        style = NitpickTheme.typography.body,
                        fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
                        color = if (isCurrent) NitpickTheme.colors.onSelection else NitpickTheme.colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun RefSectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        style = NitpickTheme.typography.kicker,
        color = NitpickTheme.colors.textMuted,
    )
}

@Composable
private fun DetailsPlaceholder(modifier: Modifier) {
    NitpickEmptyState(
        title = stringResource(Res.string.details_placeholder),
        modifier = modifier,
    )
}

@Composable
private fun PanelHint(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        style = NitpickTheme.typography.small,
        color = NitpickTheme.colors.textMuted,
    )
}

// On a detached HEAD, currentBranch is a full commit id: shorten it like the commit list does.
private fun branchLabel(currentBranch: String): String =
    if (currentBranch.length == 40 && currentBranch.all { it in "0123456789abcdef" }) {
        currentBranch.take(7)
    } else {
        currentBranch
    }

private fun folderName(path: String): String =
    path.trimEnd('/', '\\').substringAfterLast('/').substringAfterLast('\\')
