package dev.nitpick.app.ui.repository

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.SharedReview
import dev.nitpick.app.ui.components.NitpickBadge
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickButtonVariant
import dev.nitpick.app.ui.components.SyncIcon
import dev.nitpick.app.ui.components.SyncMotion
import dev.nitpick.app.ui.components.NitpickCard
import dev.nitpick.app.ui.theme.NitpickTheme
import dev.nitpick.app.ui.theme.NitpickTone
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.ic_fetch
import nitpick.shared.generated.resources.ic_share
import nitpick.shared.generated.resources.share_action
import nitpick.shared.generated.resources.share_rejected
import nitpick.shared.generated.resources.share_done
import nitpick.shared.generated.resources.share_no_remote
import nitpick.shared.generated.resources.share_unreachable
import nitpick.shared.generated.resources.shared_empty
import nitpick.shared.generated.resources.shared_refresh
import nitpick.shared.generated.resources.shared_row
import org.jetbrains.compose.resources.pluralStringResource
import nitpick.shared.generated.resources.shared_title
import org.jetbrains.compose.resources.stringResource

/** "Share review" under a review result, then a Shared badge once it is on the server. */
@Composable
internal fun ShareReviewAction(state: RepositoryUiState, onShare: () -> Unit, touch: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (state.sharedReviewId != null) {
            NitpickBadge(stringResource(Res.string.share_done), tone = NitpickTone.Success)
        } else {
            NitpickButton(
                text = stringResource(Res.string.share_action),
                onClick = onShare,
                enabled = !state.isSharing,
                variant = NitpickButtonVariant.Secondary,
                leading = { SyncIcon(Res.drawable.ic_share, SyncMotion.Up, state.isSharing, tint = NitpickTheme.colors.text) },
                size = if (touch) NitpickButtonSize.Large else NitpickButtonSize.Small,
            )
        }
        state.shareFailure?.let { ShareFailureText(it) }
    }
}

/**
 * The reviews shared for the open repository, loaded when it appears. Opening one shows its
 * nits like a local review, with no LLM call: the phone's plan B when the model is slow.
 */
@Composable
internal fun SharedReviewList(
    state: RepositoryUiState,
    onRefresh: () -> Unit,
    onOpen: (SharedReview) -> Unit,
    modifier: Modifier = Modifier,
    rowMinHeight: Dp = 32.dp,
) {
    LaunchedEffect(Unit) { onRefresh() }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.shared_title), style = NitpickTheme.typography.section, modifier = Modifier.weight(1f))
            NitpickButton(
                text = stringResource(Res.string.shared_refresh),
                onClick = onRefresh,
                enabled = !state.isLoadingSharedReviews,
                variant = NitpickButtonVariant.Ghost,
                icon = Res.drawable.ic_fetch,
                size = NitpickButtonSize.Small,
            )
        }
        when {
            state.isLoadingSharedReviews -> CircularProgressIndicator(color = NitpickTheme.colors.primary)
            state.sharedReviewsFailure != null -> ShareFailureText(state.sharedReviewsFailure)
            state.sharedReviews.isEmpty() -> Text(
                stringResource(Res.string.shared_empty),
                style = NitpickTheme.typography.small,
                color = NitpickTheme.colors.textSecondary,
            )
            else -> state.sharedReviews.forEach { shared ->
                NitpickCard(
                    modifier = Modifier.fillMaxWidth().heightIn(min = rowMinHeight).clickable { onOpen(shared) },
                    padding = 10.dp,
                ) {
                    Text(
                        text = pluralStringResource(Res.plurals.shared_row, shared.result.comments.size, shared.branch, shared.headCommit.take(7), shared.result.comments.size),
                        style = NitpickTheme.typography.codeSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = shared.result.summary,
                        style = NitpickTheme.typography.small,
                        color = NitpickTheme.colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareFailureText(failure: ShareFailure) {
    Text(
        text = when (failure) {
            ShareFailure.NoRemote -> stringResource(Res.string.share_no_remote)
            ShareFailure.Unreachable -> stringResource(Res.string.share_unreachable)
            is ShareFailure.Rejected -> stringResource(Res.string.share_rejected, failure.status)
        },
        style = NitpickTheme.typography.small,
        color = NitpickTheme.colors.danger,
    )
}
