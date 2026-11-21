package dev.nitpick.app.ui.repository

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.ui.components.NitpickBadge
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickCard
import dev.nitpick.app.ui.components.NitpickEmptyState
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.components.ReviewCommentCard
import dev.nitpick.app.ui.theme.NitpickTheme
import dev.nitpick.app.ui.theme.NitpickTone
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.review_nits_on_files
import nitpick.shared.generated.resources.commit_message
import nitpick.shared.generated.resources.commit_stage
import nitpick.shared.generated.resources.review_applied
import nitpick.shared.generated.resources.review_empty
import nitpick.shared.generated.resources.review_error
import nitpick.shared.generated.resources.review_local
import nitpick.shared.generated.resources.review_retry
import nitpick.shared.generated.resources.review_run
import nitpick.shared.generated.resources.review_running
import org.jetbrains.compose.resources.stringResource

/**
 * The Review inspector tab. The local agent is a deterministic fallback; the same tab will
 * host the Koog result without changing this contract.
 */
@Composable
fun ReviewPanel(
    state: RepositoryUiState,
    onReview: () -> Unit,
    onApply: (ReviewComment) -> Unit,
    onDismiss: (String) -> Unit,
    onCommitMessage: (String) -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        when {
            state.isReviewing -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(color = NitpickTheme.colors.primary)
                    Text(stringResource(Res.string.review_running), style = NitpickTheme.typography.small)
                }
            }
            state.reviewError != null -> NitpickEmptyState(
                title = stringResource(Res.string.review_error),
                detail = state.reviewError,
                modifier = Modifier.weight(1f),
                actionLabel = stringResource(Res.string.review_retry),
                onAction = onReview,
            )
            state.review == null -> NitpickEmptyState(
                title = stringResource(Res.string.review_empty),
                modifier = Modifier.weight(1f),
                actionLabel = stringResource(Res.string.review_run),
                onAction = onReview,
            )
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
                                onApply = if (applied) null else { { onApply(comment) } },
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
private fun CommitBar(message: String, onMessageChange: (String) -> Unit, onCommit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
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
