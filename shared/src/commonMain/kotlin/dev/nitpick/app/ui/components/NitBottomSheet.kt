package dev.nitpick.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.review_apply
import nitpick.shared.generated.resources.review_dismiss
import nitpick.shared.generated.resources.review_nit_position
import org.jetbrains.compose.resources.stringResource

private val SheetActionHeight = 56.dp

/**
 * One nit on a phone (UI handoff §4.3): the review card plus two thumb-sized actions.
 * [onApply] is null for a nit without a suggestion or one that was already applied.
 */
@Composable
fun NitSheetContent(
    comment: ReviewComment,
    position: Int,
    total: Int,
    onApply: (() -> Unit)?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // A long suggestion scrolls; the two actions stay under the thumb.
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(Res.string.review_nit_position, position, total),
                style = NitpickTheme.typography.small,
                color = NitpickTheme.colors.textMuted,
            )
            ReviewCommentCard(
                filePath = comment.filePath,
                line = comment.startLine,
                severity = comment.severity,
                message = comment.message,
                suggestion = comment.suggestion,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NitpickButton(
                text = stringResource(Res.string.review_dismiss),
                onClick = onDismiss,
                variant = NitpickButtonVariant.Secondary,
                size = NitpickButtonSize.Large,
                modifier = Modifier.weight(1f).height(SheetActionHeight),
            )
            if (onApply != null) {
                NitpickButton(
                    text = stringResource(Res.string.review_apply),
                    onClick = onApply,
                    size = NitpickButtonSize.Large,
                    modifier = Modifier.weight(1f).height(SheetActionHeight),
                )
            }
        }
    }
}

/** [NitSheetContent] in a modal sheet; [onClose] fires when the user swipes it away or taps the scrim. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NitBottomSheet(
    comment: ReviewComment,
    position: Int,
    total: Int,
    onApply: (() -> Unit)?,
    onDismiss: () -> Unit,
    onClose: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        // Half-open, a long nit hides its actions under the fold: open fully, the body scrolls.
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NitpickTheme.colors.surface,
        contentColor = NitpickTheme.colors.text,
    ) {
        NitSheetContent(comment, position, total, onApply, onDismiss)
    }
}
