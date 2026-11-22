package dev.nitpick.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.DiffLine
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.domain.Severity
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.review_dismiss
import nitpick.shared.generated.resources.review_apply
import nitpick.shared.generated.resources.review_suggested_change
import nitpick.shared.generated.resources.severity_bug
import nitpick.shared.generated.resources.severity_info
import nitpick.shared.generated.resources.severity_suggestion
import nitpick.shared.generated.resources.severity_warning
import nitpick.shared.generated.resources.review_whole_file
import org.jetbrains.compose.resources.stringResource
import dev.nitpick.app.ui.theme.NitpickTone

/**
 * Review comment card: severity is never colour alone — the badge text and the left stripe
 * carry the same information. The suggestion block is visually distinct because it is the
 * only part the user can apply with one action.
 */
@Composable
fun ReviewCommentCard(
    filePath: String,
    line: Int,
    severity: Severity,
    message: String,
    modifier: Modifier = Modifier,
    suggestion: String? = null,
    onApply: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = NitpickTheme.colors
    val tone = severityTone(severity)
    NitpickCard(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NitpickBadge(text = severityLabel(severity), tone = tone)
            Text(
                text = if (line == 0) "$filePath · ${stringResource(Res.string.review_whole_file)}" else "$filePath:$line",
                style = NitpickTheme.typography.codeSmall,
                color = colors.textMuted,
                maxLines = 1,
                // A long path loses its folders, never the file name and line that anchor the nit.
                overflow = TextOverflow.StartEllipsis,
                modifier = Modifier.weight(1f),
            )
            if (onDismiss != null) {
                NitpickButton(
                    text = stringResource(Res.string.review_dismiss),
                    onClick = onDismiss,
                    variant = NitpickButtonVariant.Ghost,
                    size = NitpickButtonSize.Small,
                )
            }
        }
        Text(text = message, style = NitpickTheme.typography.body, color = colors.text)
        if (suggestion != null) {
            Surface(
                shape = RoundedCornerShape(NitpickTheme.radii.sm),
                color = colors.surfaceSunken,
                contentColor = colors.text,
                border = BorderStroke(1.dp, colors.border),
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.review_suggested_change),
                        style = NitpickTheme.typography.kicker,
                        color = colors.textMuted,
                    )
                    Text(text = suggestion, style = NitpickTheme.typography.code, color = colors.text)
                    if (onApply != null) {
                        NitpickButton(
                            text = stringResource(Res.string.review_apply),
                            onClick = onApply,
                            size = NitpickButtonSize.Small,
                        )
                    }
                }
            }
        }
    }
}

/**
 * One diff line. The `+`/`-` gutter and the line-number columns are always present so the
 * meaning survives colour-blindness and monochrome screenshots.
 */
@Composable
fun DiffLineRow(line: DiffLine, modifier: Modifier = Modifier) {
    val colors = NitpickTheme.colors
    val (background, foreground, gutter) = when (line.type) {
        DiffLineType.ADDED -> Triple(colors.diffAddedBg, colors.diffAddedFg, "+")
        DiffLineType.REMOVED -> Triple(colors.diffRemovedBg, colors.diffRemovedFg, "-")
        DiffLineType.CONTEXT -> Triple(colors.diffContextBg, colors.text, " ")
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 20.dp)
            .background(background)
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DiffLineNumber(line.oldLineNumber?.toString().orEmpty(), colors.lineNumber)
        DiffLineNumber(line.newLineNumber?.toString().orEmpty(), colors.lineNumber)
        Text(
            text = gutter,
            modifier = Modifier.width(16.dp),
            style = NitpickTheme.typography.codeSmall,
            color = foreground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = line.content,
            style = NitpickTheme.typography.codeSmall,
            color = foreground,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun DiffLineNumber(value: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = value,
        modifier = Modifier.width(40.dp),
        style = NitpickTheme.typography.codeSmall,
        color = color,
        textAlign = TextAlign.End,
        maxLines = 1,
    )
}

@Composable
fun RefBadge(ref: Ref, modifier: Modifier = Modifier) {
    val tone = when (ref.type) {
        RefType.LOCAL_BRANCH -> NitpickTone.Accent
        RefType.REMOTE_BRANCH -> NitpickTone.Neutral
        RefType.TAG -> NitpickTone.Warning
    }
    val prefix = when (ref.type) {
        RefType.LOCAL_BRANCH -> "branch"
        RefType.REMOTE_BRANCH -> "remote"
        RefType.TAG -> "tag"
    }
    NitpickBadge(text = "$prefix · ${ref.name}", tone = tone, modifier = modifier)
}

internal fun severityTone(severity: Severity): NitpickTone = when (severity) {
    Severity.INFO -> NitpickTone.Info
    Severity.SUGGESTION -> NitpickTone.Suggestion
    Severity.WARNING -> NitpickTone.Warning
    Severity.BUG -> NitpickTone.Danger
}

@Composable
internal fun severityLabel(severity: Severity): String = when (severity) {
    Severity.INFO -> stringResource(Res.string.severity_info)
    Severity.SUGGESTION -> stringResource(Res.string.severity_suggestion)
    Severity.WARNING -> stringResource(Res.string.severity_warning)
    Severity.BUG -> stringResource(Res.string.severity_bug)
}
