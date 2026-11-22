package dev.nitpick.app.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import dev.nitpick.app.ui.components.NitpickIcon
import nitpick.shared.generated.resources.ic_fetch
import nitpick.shared.generated.resources.ic_pull
import nitpick.shared.generated.resources.ic_push
import nitpick.shared.generated.resources.ic_tweezers
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickButtonVariant
import dev.nitpick.app.ui.components.NitpickCard
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
import nitpick.shared.generated.resources.branch_no_upstream
import nitpick.shared.generated.resources.fetch_action
import nitpick.shared.generated.resources.ic_cloud
import nitpick.shared.generated.resources.fetch_in_progress
import nitpick.shared.generated.resources.pull_action
import nitpick.shared.generated.resources.pull_in_progress
import nitpick.shared.generated.resources.publish_action
import nitpick.shared.generated.resources.publish_in_progress
import nitpick.shared.generated.resources.push_action
import nitpick.shared.generated.resources.push_in_progress
import nitpick.shared.generated.resources.repository_loading
import nitpick.shared.generated.resources.review_cta
import nitpick.shared.generated.resources.shared_open
import nitpick.shared.generated.resources.commits_empty
import nitpick.shared.generated.resources.commits_title
import nitpick.shared.generated.resources.refs_local_branches
import nitpick.shared.generated.resources.repository_error_title
import nitpick.shared.generated.resources.status_commits
import nitpick.shared.generated.resources.sync_ahead_behind
import nitpick.shared.generated.resources.sync_pull_count
import nitpick.shared.generated.resources.sync_push_count
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private enum class GraphTab { Commits, Branches }

/** Touch target of the sync actions on the phone (UI handoff §4.2: 44 dp). */
private val SyncActionHeight = 44.dp

/** Commits and branches of the open repository, one tab at a time (UI handoff §4). */
@Composable
fun PhoneGraphScreen(
    state: RepositoryUiState,
    onBack: () -> Unit,
    onSelectCommit: (String) -> Unit,
    onCheckoutBranch: (String) -> Unit,
    onPull: () -> Unit,
    onPush: () -> Unit,
    onPublish: () -> Unit,
    onFetch: () -> Unit,
    onReview: () -> Unit,
    onOpenShared: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        PhoneTopBar(title = state.path?.let(::folderName) ?: "", onBack = onBack) {
            PhoneIconAction(Res.drawable.ic_cloud, stringResource(Res.string.shared_open), onOpenShared)
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
                BranchCard(state, onFetch, onPull, onPush, onPublish, Modifier.fillMaxWidth().padding(16.dp))
                GraphContent(state, onSelectCommit, onCheckoutBranch, Modifier.weight(1f))
                syncStatusMessage(state)?.let { message ->
                    NitpickDivider()
                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        style = NitpickTheme.typography.small,
                        color = if (state.hasSyncProblem()) NitpickTheme.colors.danger else NitpickTheme.colors.success,
                    )
                }
                PhoneBottomButton(
                    text = stringResource(if (state.isCheckingOut) Res.string.repository_loading else Res.string.review_cta),
                    onClick = onReview,
                    enabled = !state.isCheckingOut,
                    icon = Res.drawable.ic_tweezers,
                )
            }
        }
    }
}

/** Current branch, its upstream with ↑n ↓m, and the three 44 dp sync actions (UI handoff §4.2). */
@Composable
private fun BranchCard(
    state: RepositoryUiState,
    onFetch: () -> Unit,
    onPull: () -> Unit,
    onPush: () -> Unit,
    onPublish: () -> Unit,
    modifier: Modifier,
) {
    NitpickCard(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = branchLabel(state.currentBranch),
                style = NitpickTheme.typography.code,
                color = NitpickTheme.colors.text,
                modifier = Modifier.weight(1f, fill = false),
            )
            Text(
                text = pluralStringResource(Res.plurals.status_commits, state.graph.size, state.graph.size),
                style = NitpickTheme.typography.small,
                color = NitpickTheme.colors.textMuted,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = state.tracking?.upstream ?: stringResource(Res.string.branch_no_upstream),
                style = NitpickTheme.typography.small,
                color = NitpickTheme.colors.textMuted,
                modifier = Modifier.weight(1f, fill = false),
            )
            state.tracking?.let {
                Text(
                    text = stringResource(Res.string.sync_ahead_behind, it.ahead, it.behind),
                    style = NitpickTheme.typography.codeSmall,
                    color = NitpickTheme.colors.textMuted,
                )
            }
        }
        val enabled = !state.isSyncing
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SyncButton(
                text = stringResource(if (state.isFetching) Res.string.fetch_in_progress else Res.string.fetch_action),
                icon = Res.drawable.ic_fetch,
                onClick = onFetch,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            SyncButton(
                text = when {
                    state.isPulling -> stringResource(Res.string.pull_in_progress)
                    (state.tracking?.behind ?: 0) > 0 -> stringResource(Res.string.sync_pull_count, state.tracking!!.behind)
                    else -> stringResource(Res.string.pull_action)
                },
                icon = Res.drawable.ic_pull,
                onClick = onPull,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            if (state.tracking != null || state.hasOrigin) {
                SyncButton(
                    text = when {
                        state.isPushing -> stringResource(Res.string.push_in_progress)
                        state.tracking == null -> stringResource(Res.string.publish_action)
                        state.tracking.ahead > 0 -> stringResource(Res.string.sync_push_count, state.tracking.ahead)
                        else -> stringResource(Res.string.push_action)
                    },
                    icon = Res.drawable.ic_push,
                    onClick = if (state.tracking == null) onPublish else onPush,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SyncButton(text: String, icon: DrawableResource, onClick: () -> Unit, enabled: Boolean, modifier: Modifier) {
    NitpickButton(
        text = text,
        onClick = onClick,
        modifier = modifier.height(SyncActionHeight),
        variant = NitpickButtonVariant.Secondary,
        size = NitpickButtonSize.Large,
        enabled = enabled,
        leading = { NitpickIcon(icon, contentDescription = null, tint = NitpickTheme.colors.text) },
    )
}

@Composable
private fun GraphContent(state: RepositoryUiState, onSelectCommit: (String) -> Unit, onCheckoutBranch: (String) -> Unit, modifier: Modifier) {
    var tab by rememberSaveable { mutableStateOf(GraphTab.Commits) }
    Column(modifier) {
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
            RefsSidebar(state.refs, state.currentBranch, Modifier.fillMaxSize(), ahead = state.tracking?.ahead, onSelectBranch = onCheckoutBranch)
        }
        state.graph.isEmpty() -> Text(
            text = stringResource(Res.string.commits_empty),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            style = NitpickTheme.typography.small,
            color = NitpickTheme.colors.textMuted,
        )
        else -> CommitGraphList(state.graph, state.selectedCommitId, onSelectCommit, Modifier.fillMaxSize(), compactTime = true)
    }
    }
}
