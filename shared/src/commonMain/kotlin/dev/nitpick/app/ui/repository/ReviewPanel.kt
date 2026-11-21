package dev.nitpick.app.ui.repository

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.ui.components.NitpickBadge
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickButtonVariant
import dev.nitpick.app.ui.components.NitpickCard
import dev.nitpick.app.ui.components.NitpickEmptyState
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.components.ReviewCommentCard
import dev.nitpick.app.ui.theme.NitpickTheme
import dev.nitpick.app.ui.theme.NitpickTone
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.commit_message
import nitpick.shared.generated.resources.commit_stage
import nitpick.shared.generated.resources.error_key_action
import nitpick.shared.generated.resources.error_key_body
import nitpick.shared.generated.resources.error_key_title
import nitpick.shared.generated.resources.error_network_body
import nitpick.shared.generated.resources.error_network_title
import nitpick.shared.generated.resources.error_timeout_body
import nitpick.shared.generated.resources.error_timeout_title
import nitpick.shared.generated.resources.review_applied
import nitpick.shared.generated.resources.review_clean_body
import nitpick.shared.generated.resources.review_clean_title
import nitpick.shared.generated.resources.review_empty
import nitpick.shared.generated.resources.review_error
import nitpick.shared.generated.resources.review_local
import nitpick.shared.generated.resources.review_nits_on_files
import nitpick.shared.generated.resources.review_retry
import nitpick.shared.generated.resources.review_run
import nitpick.shared.generated.resources.review_running
import nitpick.shared.generated.resources.review_step_diff
import nitpick.shared.generated.resources.review_step_model
import nitpick.shared.generated.resources.review_step_place
import nitpick.shared.generated.resources.review_step_read
import org.jetbrains.compose.resources.stringResource

/**
 * The Review inspector tab. It renders the agent result and the six handoff states without
 * knowing which provider produced it; the typed [ReviewFailure] keeps missing-key, network
 * and timeout messages actionable.
 */
@Composable
fun ReviewPanel(
    state: RepositoryUiState,
    onReview: () -> Unit,
    onApply: (ReviewComment) -> Unit,
    onDismiss: (String) -> Unit,
    onCommitMessage: (String) -> Unit,
    onCommit: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        when {
            state.isReviewing -> ReviewProgress()
            state.reviewError != null -> ReviewFailureState(
                failure = state.reviewError,
                onRetry = onReview,
                onOpenSettings = onOpenSettings,
            )
            state.review == null -> NitpickEmptyState(
                title = stringResource(Res.string.review_empty),
                modifier = Modifier.weight(1f),
                actionLabel = stringResource(Res.string.review_run),
                onAction = onReview,
            )
            state.review.comments.isEmpty() -> CleanReviewState()
            else -> {
                val review = requireNotNull(state.review)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        NitpickCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                NitpickBadge(stringResource(Res.string.review_local), tone = NitpickTone.Info)
                                Text(review.summary, style = NitpickTheme.typography.section)
                            }
                            Text(
                                stringResource(Res.string.review_nits_on_files, review.comments.size, state.diff.size),
                                style = NitpickTheme.typography.small,
                                color = NitpickTheme.colors.textSecondary,
                            )
                        }
                    }
                    items(review.comments, key = { it.id }) { comment ->
                        val applied = comment.id in state.appliedIds
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (applied) {
                                NitpickBadge(stringResource(Res.string.review_applied), tone = NitpickTone.Success)
                            }
                            ReviewCommentCard(
                                filePath = comment.filePath,
                                line = comment.startLine,
                                severity = comment.severity,
                                message = comment.message,
                                suggestion = comment.suggestion,
                                onApply = if (applied || comment.isFileLevel) null else { { onApply(comment) } },
                                onDismiss = if (applied) null else { { onDismiss(comment.id) } },
                            )
                        }
                    }
                }
                if (state.appliedFiles.isNotEmpty()) {
                    CommitBar(
                        message = state.commitMessage,
                        onMessageChange = onCommitMessage,
                        onCommit = onCommit,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewProgress() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NitpickCard(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NitpickBadge("AI", tone = NitpickTone.Ai)
                Text(stringResource(Res.string.review_running), style = NitpickTheme.typography.section)
            }
            CircularProgressIndicator(color = NitpickTheme.colors.primary, modifier = Modifier.padding(start = 2.dp))
            listOf(
                Res.string.review_step_diff,
                Res.string.review_step_read,
                Res.string.review_step_model,
                Res.string.review_step_place,
            ).forEach { step ->
                Text(stringResource(step), style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
            }
        }
    }
}

@Composable
private fun ReviewFailureState(
    failure: ReviewFailure,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val (tone, title, body, actionLabel, action) = when (failure) {
        is ReviewFailure.MissingKey -> FailureContent(
            NitpickTone.Warning,
            stringResource(Res.string.error_key_title),
            stringResource(Res.string.error_key_body),
            stringResource(Res.string.error_key_action),
            onOpenSettings,
        )
        ReviewFailure.Network -> FailureContent(
            NitpickTone.Danger,
            stringResource(Res.string.error_network_title),
            stringResource(Res.string.error_network_body),
            stringResource(Res.string.review_retry),
            onRetry,
        )
        ReviewFailure.Timeout -> FailureContent(
            NitpickTone.Danger,
            stringResource(Res.string.error_timeout_title),
            stringResource(Res.string.error_timeout_body),
            stringResource(Res.string.review_retry),
            onRetry,
        )
        is ReviewFailure.Other -> FailureContent(
            NitpickTone.Danger,
            stringResource(Res.string.review_error),
            failure.message,
            stringResource(Res.string.review_retry),
            onRetry,
        )
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NitpickCard(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NitpickBadge(title, tone = tone)
            Text(body, style = NitpickTheme.typography.body, color = NitpickTheme.colors.text)
            NitpickButton(text = actionLabel, onClick = action, size = NitpickButtonSize.Small)
        }
    }
}

private data class FailureContent(
    val tone: NitpickTone,
    val title: String,
    val body: String,
    val actionLabel: String,
    val action: () -> Unit,
)

@Composable
private fun CleanReviewState() {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        NitpickBadge("CLEAN", tone = NitpickTone.Success)
        Text(
            stringResource(Res.string.review_clean_title),
            modifier = Modifier.padding(top = 12.dp),
            style = NitpickTheme.typography.section,
            color = NitpickTheme.colors.text,
        )
        Text(
            stringResource(Res.string.review_clean_body),
            modifier = Modifier.padding(top = 6.dp),
            style = NitpickTheme.typography.small,
            color = NitpickTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun CommitBar(message: String, onMessageChange: (String) -> Unit, onCommit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NitpickTextField(
            value = message,
            onValueChange = onMessageChange,
            placeholder = stringResource(Res.string.commit_message),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            NitpickButton(
                text = stringResource(Res.string.commit_stage),
                onClick = onCommit,
                size = NitpickButtonSize.Small,
            )
        }
    }
}
