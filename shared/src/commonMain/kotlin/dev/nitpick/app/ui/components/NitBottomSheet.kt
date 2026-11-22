package dev.nitpick.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import nitpick.shared.generated.resources.review_apply
import nitpick.shared.generated.resources.review_dismiss
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import nitpick.shared.generated.resources.ic_check
import nitpick.shared.generated.resources.ic_x
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.review_nit_position
import org.jetbrains.compose.resources.stringResource

private val SheetActionHeight = 56.dp
private val SheetIconSize = 24.dp

/**
 * One nit on a phone (UI handoff §4.3): the review card, then a thumb-sized cross and check.
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
        // A long suggestion scrolls inside the sheet.
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(Res.string.review_nit_position, position, total),
                style = NitpickTheme.typography.small,
                color = NitpickTheme.colors.textMuted,
            )
            // No squares on the phone card: the two big buttons below carry the check and the cross.
            ReviewCommentCard(
                filePath = comment.filePath,
                line = comment.startLine,
                severity = comment.severity,
                message = comment.message,
                suggestion = comment.suggestion,
                original = comment.originalSnippet,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val colors = NitpickTheme.colors
            SheetIconButton(Res.drawable.ic_x, stringResource(Res.string.review_dismiss), colors.dangerSoft, colors.danger, onDismiss, Modifier.weight(1f))
            if (onApply != null) {
                SheetIconButton(Res.drawable.ic_check, stringResource(Res.string.review_apply), colors.success, colors.onPrimary, onApply, Modifier.weight(1f))
            }
        }
    }
}

/** A thumb-sized button that is only an icon; the description is what TalkBack reads. */
@Composable
private fun SheetIconButton(icon: DrawableResource, description: String, background: Color, tint: Color, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier.height(SheetActionHeight).clip(RoundedCornerShape(NitpickTheme.radii.md)).background(background).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NitpickIcon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(SheetIconSize))
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
