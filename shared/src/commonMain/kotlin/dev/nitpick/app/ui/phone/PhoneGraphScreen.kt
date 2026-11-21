package dev.nitpick.app.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.components.NitpickDivider
import dev.nitpick.app.ui.components.NitpickEmptyState
import dev.nitpick.app.ui.components.NitpickTab
import dev.nitpick.app.ui.components.NitpickToolbarAction
import dev.nitpick.app.ui.repository.CommitGraphList
import dev.nitpick.app.ui.repository.RefsSidebar
import dev.nitpick.app.ui.repository.RepositoryUiState
import dev.nitpick.app.ui.repository.branchLabel
import dev.nitpick.app.ui.repository.folderName
import dev.nitpick.app.ui.repository.hasSyncProblem
import dev.nitpick.app.ui.repository.syncStatusMessage
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.action_back
import nitpick.shared.generated.resources.branch_checkout_hint
import nitpick.shared.generated.resources.pull_action
import nitpick.shared.generated.resources.pull_in_progress
import nitpick.shared.generated.resources.repository_loading
import nitpick.shared.generated.resources.review_cta
import nitpick.shared.generated.resources.commits_empty
import nitpick.shared.generated.resources.commits_title
import nitpick.shared.generated.resources.refs_local_branches
import nitpick.shared.generated.resources.repository_error_title
import nitpick.shared.generated.resources.repository_on_branch
import nitpick.shared.generated.resources.status_commits
import org.jetbrains.compose.resources.stringResource

private enum class GraphTab { Commits, Branches }

/** Commits and branches of the open repository, one tab at a time (UI handoff §4). */
@Composable
fun PhoneGraphScreen(
    state: RepositoryUiState,
    onBack: () -> Unit,
    onSelectCommit: (String) -> Unit,
    onCheckoutBranch: (String) -> Unit,
    onPull: () -> Unit,
    onReview: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        PhoneTopBar(title = state.path?.let(::folderName) ?: "", onBack = onBack) {
            if (state.path != null && state.errorMessage == null && !state.isLoading) {
                NitpickToolbarAction(
                    text = stringResource(if (state.isPulling) Res.string.pull_in_progress else Res.string.pull_action),
                    onClick = { if (!state.isPulling) onPull() },
                )
            }
            SettingsAction(onOpenSettings)
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NitpickTheme.colors.primary)
            }
            state.errorMessage != null -> NitpickEmptyState(
                title = stringResource(Res.string.repository_error_title),
                detail = state.errorMessage,
                actionLabel = stringResource(Res.string.action_back),
                onAction = onBack,
            )
            else -> {
                GraphContent(state, onSelectCommit, onCheckoutBranch, Modifier.weight(1f))
                syncStatusMessage(state)?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        style = NitpickTheme.typography.small,
                        color = if (state.hasSyncProblem()) NitpickTheme.colors.danger else NitpickTheme.colors.success,
                    )
                }
                PhoneBottomButton(
                    text = stringResource(if (state.isCheckingOut) Res.string.repository_loading else Res.string.review_cta),
                    onClick = onReview,
                    enabled = !state.isCheckingOut,
                )
            }
        }
    }
}

@Composable
private fun GraphContent(state: RepositoryUiState, onSelectCommit: (String) -> Unit, onCheckoutBranch: (String) -> Unit, modifier: Modifier) {
    var tab by rememberSaveable { mutableStateOf(GraphTab.Commits) }
    Column(modifier) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.repository_on_branch, branchLabel(state.currentBranch)),
            style = NitpickTheme.typography.bodyStrong,
            color = NitpickTheme.colors.text,
        )
        Text(
            text = stringResource(Res.string.status_commits, state.graph.size),
            style = NitpickTheme.typography.body,
            color = NitpickTheme.colors.textMuted,
        )
    }
    Row(Modifier.fillMaxWidth().background(NitpickTheme.colors.panelHeader).padding(horizontal = 4.dp), verticalAlignment = Alignment.Bottom) {
        NitpickTab(stringResource(Res.string.commits_title), selected = tab == GraphTab.Commits, onClick = { tab = GraphTab.Commits })
        NitpickTab(stringResource(Res.string.refs_local_branches), selected = tab == GraphTab.Branches, onClick = { tab = GraphTab.Branches })
    }
    NitpickDivider()
    when {
        tab == GraphTab.Branches -> Column(Modifier.fillMaxSize()) {
            Text(
                text = stringResource(Res.string.branch_checkout_hint),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                style = NitpickTheme.typography.micro,
                color = NitpickTheme.colors.textMuted,
            )
            RefsSidebar(state.refs, state.currentBranch, Modifier.fillMaxSize(), onSelectBranch = onCheckoutBranch)
        }
        state.graph.isEmpty() -> Text(
            text = stringResource(Res.string.commits_empty),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            style = NitpickTheme.typography.small,
            color = NitpickTheme.colors.textMuted,
        )
        else -> CommitGraphList(state.graph, state.selectedCommitId, onSelectCommit, Modifier.fillMaxSize())
    }
    }
}
