package dev.nitpick.app.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.ui.components.NitBottomSheet
import dev.nitpick.app.ui.components.NitpickBadge
import dev.nitpick.app.ui.components.NitpickCard
import dev.nitpick.app.ui.components.NitpickDivider
import dev.nitpick.app.ui.components.NitpickEmptyState
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.components.NitpickToolbarAction
import dev.nitpick.app.ui.components.severityLabel
import dev.nitpick.app.ui.components.severityTone
import dev.nitpick.app.ui.repository.RepositoryUiState
import dev.nitpick.app.ui.repository.ReviewFailureState
import dev.nitpick.app.ui.repository.ShareReviewAction
import dev.nitpick.app.ui.repository.branchLabel
import dev.nitpick.app.ui.theme.NitpickTheme
import dev.nitpick.app.ui.theme.NitpickTone
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.commit_bar_applied
import nitpick.shared.generated.resources.commit_message
import nitpick.shared.generated.resources.commit_stage
import nitpick.shared.generated.resources.push_action
import nitpick.shared.generated.resources.push_error
import nitpick.shared.generated.resources.push_in_progress
import nitpick.shared.generated.resources.review_applied
import nitpick.shared.generated.resources.review_clean_title
import nitpick.shared.generated.resources.review_empty
import nitpick.shared.generated.resources.review_files_with_nits
import nitpick.shared.generated.resources.review_line
import nitpick.shared.generated.resources.review_local
import nitpick.shared.generated.resources.review_rerun
import nitpick.shared.generated.resources.review_run
import nitpick.shared.generated.resources.review_running
import nitpick.shared.generated.resources.review_title
import nitpick.shared.generated.resources.review_vs
import nitpick.shared.generated.resources.review_whole_file_short
import nitpick.shared.generated.resources.shared_badge
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val NitRowHeight = 56.dp

/** Review on a phone (UI handoff §4.3): summary, nits grouped by file, then commit and push. */
@Composable
fun PhoneReviewScreen(
    state: RepositoryUiState,
    onBack: () -> Unit,
    onRunReview: () -> Unit,
    onOpenFile: (String) -> Unit,
    onApply: (ReviewComment) -> Unit,
    onDismiss: (String) -> Unit,
    onCommitMessage: (String) -> Unit,
    onCommit: () -> Unit,
    onPush: () -> Unit,
    onShare: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var openNitId by rememberSaveable { mutableStateOf<String?>(null) }
    val review = state.review

    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        PhoneTopBar(
            title = stringResource(Res.string.review_title) + " · " + stringResource(Res.string.review_vs, branchLabel(state.currentBranch), state.baseRef),
            onBack = onBack,
        ) {
            if (review != null && !state.isReviewing) {
                NitpickToolbarAction(text = stringResource(Res.string.review_rerun), onClick = onRunReview)
            }
        }
        Box(Modifier.weight(1f)) {
            when {
                // Opening a shared review may check its branch out first.
                state.isReviewing || state.isCheckingOut -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = NitpickTheme.colors.primary)
                        Text(stringResource(Res.string.review_running), style = NitpickTheme.typography.small)
                    }
                }
                state.reviewError != null -> ReviewFailureState(
                    failure = state.reviewError,
                    onRetry = onRunReview,
                    onOpenSettings = onOpenSettings,
                )
                review == null -> NitpickEmptyState(
                    title = stringResource(Res.string.review_empty),
                    actionLabel = stringResource(Res.string.review_run),
                    onAction = onRunReview,
                )
                else -> NitList(state, review.summary, review.comments, onOpenFile, onOpenNit = { openNitId = it }, onShare, Modifier.fillMaxSize())
            }
        }
        // Outside the commit bar, which is hidden while nothing is applied: a first apply that fails must still show why.
        state.actionError?.let { error ->
            Text(
                text = error,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                style = NitpickTheme.typography.small,
                color = NitpickTheme.colors.danger,
            )
        }
        CommitAndPushBar(state, onCommitMessage, onCommit, onPush)
    }

    val open = review?.comments?.firstOrNull { it.id == openNitId }
    if (open != null) {
        val applied = open.id in state.appliedIds
        NitBottomSheet(
            comment = open,
            position = review.comments.indexOf(open) + 1,
            total = review.comments.size,
            onApply = if (applied || open.suggestion == null || open.isFileLevel || state.isRewritingWorkingTree) null else ({ onApply(open); openNitId = null }),
            onDismiss = { onDismiss(open.id); openNitId = null },
            onClose = { openNitId = null },
        )
    }
}

@Composable
private fun NitList(
    state: RepositoryUiState,
    summary: String,
    comments: List<ReviewComment>,
    onOpenFile: (String) -> Unit,
    onOpenNit: (String) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier,
) {
    val byFile = remember(comments) { comments.groupBy { it.filePath } }
    LazyColumn(modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            NitpickCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NitpickBadge(
                        stringResource(if (state.openedSharedReview != null) Res.string.shared_badge else Res.string.review_local),
                        tone = NitpickTone.Info,
                    )
                    Text(summary, style = NitpickTheme.typography.section, modifier = Modifier.weight(1f))
                }
                Text(
                    text = if (comments.isEmpty()) stringResource(Res.string.review_clean_title) else stringResource(Res.string.review_files_with_nits, byFile.size),
                    style = NitpickTheme.typography.small,
                    color = NitpickTheme.colors.textSecondary,
                )
                ShareReviewAction(state, onShare, touch = true)
            }
        }
        byFile.forEach { (path, nits) ->
            item(key = "file:$path") { FileHeader(path, onClick = { onOpenFile(path) }) }
            items(nits, key = { it.id }) { nit -> NitRow(nit, applied = nit.id in state.appliedIds, onClick = { onOpenNit(nit.id) }) }
        }
    }
}

@Composable
private fun FileHeader(path: String, onClick: () -> Unit) {
    Text(
        text = "$path ›",
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(top = 8.dp, bottom = 4.dp),
        style = NitpickTheme.typography.codeSmall,
        color = NitpickTheme.colors.textSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun NitRow(nit: ReviewComment, applied: Boolean, onClick: () -> Unit) {
    NitpickCard(modifier = Modifier.fillMaxWidth().heightIn(min = NitRowHeight).clickable(onClick = onClick), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NitpickBadge(severityLabel(nit.severity), tone = severityTone(nit.severity))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(nit.message, style = NitpickTheme.typography.body, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    text = if (nit.isFileLevel) stringResource(Res.string.review_whole_file_short) else stringResource(Res.string.review_line, nit.startLine),
                    style = NitpickTheme.typography.micro,
                    color = NitpickTheme.colors.textMuted,
                )
            }
            if (applied) NitpickBadge(stringResource(Res.string.review_applied), tone = NitpickTone.Success) else Text("›", style = NitpickTheme.typography.title, color = NitpickTheme.colors.textMuted)
        }
    }
}

/** Commit the applied suggestions, then push: the bottom of the screen walks the user through both. */
@Composable
private fun CommitAndPushBar(
    state: RepositoryUiState,
    onCommitMessage: (String) -> Unit,
    onCommit: () -> Unit,
    onPush: () -> Unit,
) {
    if (state.appliedFiles.isEmpty() && !state.isPushing && !state.hasUnpushedCommits) return
    NitpickDivider()
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            state.appliedFiles.isNotEmpty() -> {
                Text(pluralStringResource(Res.plurals.commit_bar_applied, state.appliedIds.size, state.appliedIds.size), style = NitpickTheme.typography.small, color = NitpickTheme.colors.success)
                NitpickTextField(value = state.commitMessage, onValueChange = onCommitMessage, placeholder = stringResource(Res.string.commit_message))
                PhoneBottomButton(text = stringResource(Res.string.commit_stage), onClick = onCommit, enabled = state.commitMessage.isNotBlank() && !state.isRewritingWorkingTree)
            }
            state.isPushing -> PhoneBottomButton(text = stringResource(Res.string.push_in_progress), onClick = {}, enabled = false)
            else -> {
                if (state.pushError != null) {
                    Text(stringResource(Res.string.push_error) + ": " + state.pushError, style = NitpickTheme.typography.small, color = NitpickTheme.colors.danger)
                }
                PhoneBottomButton(text = stringResource(Res.string.push_action), onClick = onPush)
            }
        }
    }
}
