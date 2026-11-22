package dev.nitpick.app.ui.repository

import androidx.compose.runtime.Composable
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.fetch_error
import nitpick.shared.generated.resources.label_detail
import nitpick.shared.generated.resources.pull_conflict
import nitpick.shared.generated.resources.pull_error
import nitpick.shared.generated.resources.pull_up_to_date
import nitpick.shared.generated.resources.pull_updated
import nitpick.shared.generated.resources.push_done
import nitpick.shared.generated.resources.push_error
import nitpick.shared.generated.resources.sync_commit_first
import org.jetbrains.compose.resources.stringResource

/** One line about the last sync or checkout, for the desktop status bar and the phone Graph screen. */
@Composable
internal fun syncStatusMessage(state: RepositoryUiState): String? = when {
    state.fetchError != null -> stringResource(Res.string.label_detail, stringResource(Res.string.fetch_error), state.fetchError)
    state.pullError != null -> stringResource(Res.string.label_detail, stringResource(Res.string.pull_error), state.pullError)
    state.pushError != null -> stringResource(Res.string.label_detail, stringResource(Res.string.push_error), state.pushError)
    state.checkoutError != null -> state.checkoutError
    state.appliedFiles.isNotEmpty() -> stringResource(Res.string.sync_commit_first)
    state.lastPushedBranch != null -> stringResource(Res.string.push_done, state.lastPushedBranch)
    state.lastPull == null -> null
    state.lastPull.conflictedPaths.isNotEmpty() -> stringResource(Res.string.pull_conflict, state.lastPull.conflictedPaths.joinToString(", "))
    state.lastPull.updated -> stringResource(Res.string.pull_updated)
    else -> stringResource(Res.string.pull_up_to_date)
}

/** True while a sync error or a conflict should be shown in the danger colour. */
internal fun RepositoryUiState.hasSyncProblem(): Boolean =
    fetchError != null || pullError != null || pushError != null || checkoutError != null || lastPull?.conflictedPaths?.isNotEmpty() == true
