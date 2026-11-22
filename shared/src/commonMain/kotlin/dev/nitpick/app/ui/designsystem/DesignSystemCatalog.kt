package dev.nitpick.app.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.DiffLine
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.Severity
import dev.nitpick.app.ui.components.DiffLineRow
import dev.nitpick.app.ui.components.NitpickBadge
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickButtonVariant
import dev.nitpick.app.ui.components.NitpickCard
import dev.nitpick.app.ui.components.NitpickKbd
import dev.nitpick.app.ui.components.NitpickRadioGroup
import dev.nitpick.app.ui.components.NitpickReviewLoader
import dev.nitpick.app.ui.components.NitpickSegmentedControl
import dev.nitpick.app.ui.components.NitpickSectionHeader
import dev.nitpick.app.ui.components.NitpickStatusBar
import dev.nitpick.app.ui.components.NitpickTab
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.components.NitpickToolbar
import dev.nitpick.app.ui.components.NitpickToolbarAction
import dev.nitpick.app.ui.components.NitpickTooltip
import dev.nitpick.app.ui.components.NitpickToolWindowHeader
import dev.nitpick.app.ui.components.RefBadge
import dev.nitpick.app.ui.components.NitSheetContent
import dev.nitpick.app.ui.components.ReviewCommentCard
import dev.nitpick.app.ui.theme.NitpickTheme
import dev.nitpick.app.ui.theme.NitpickTone

/**
 * Visual catalogue of the design system. It is not part of the product navigation: open it
 * from the IDE preview or temporarily route App() here when changing tokens.
 */
@Composable
fun DesignSystemCatalog(darkTheme: Boolean = isSystemInDarkTheme()) {
    NitpickTheme(darkTheme = darkTheme) {
        LazyColumn(
            modifier = Modifier.background(NitpickTheme.colors.canvas),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                NitpickSectionHeader(
                    kicker = "JetBrains New UI",
                    title = "Nitpick design system",
                    subtitle = "Dense IDE chrome, one teal action, colour only where it means something.",
                )
            }
            item { ColourSection() }
            item { TypographySection() }
            item { ControlsSection() }
            item { ReviewSection() }
            item { ShellSection() }
        }
    }
}

@Composable
private fun ColourSection() {
    NitpickCard(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NitpickSectionHeader(title = "Colour", kicker = "Semantic tokens")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Swatch("canvas", NitpickTheme.colors.canvas)
            Swatch("panel", NitpickTheme.colors.surface)
            Swatch("border", NitpickTheme.colors.border)
            Swatch("text", NitpickTheme.colors.text)
            Swatch("action", NitpickTheme.colors.primary)
            Swatch("hover", NitpickTheme.colors.primaryHover)
            Swatch("selection", NitpickTheme.colors.selection)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Swatch("added", NitpickTheme.colors.diffAddedBg)
            Swatch("removed", NitpickTheme.colors.diffRemovedBg)
            Swatch("bug", NitpickTheme.colors.danger)
            Swatch("warning", NitpickTheme.colors.warning)
            Swatch("info", NitpickTheme.colors.info)
            Swatch("AI", NitpickTheme.colors.ai)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NitpickTheme.colors.lanePalette.forEach { lane ->
                Box(Modifier.size(24.dp).background(lane, RoundedCornerShape(NitpickTheme.radii.xs)))
            }
        }
    }
}

@Composable
private fun Swatch(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .size(width = 52.dp, height = 30.dp)
                .background(color, RoundedCornerShape(NitpickTheme.radii.sm)),
        )
        Text(label, style = NitpickTheme.typography.micro, color = NitpickTheme.colors.textMuted)
    }
}

@Composable
private fun TypographySection() {
    NitpickCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NitpickSectionHeader(title = "Typography", kicker = "Inter / JetBrains Mono")
        Text("Display 24 · IDE page title", style = NitpickTheme.typography.display)
        Text("Section 15 · tool window section", style = NitpickTheme.typography.section)
        Text("Body 13 · review message and controls", style = NitpickTheme.typography.body)
        Text("Small 12 · metadata", style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
        Text("KICKER 11 · group label", style = NitpickTheme.typography.kicker, color = NitpickTheme.colors.textMuted)
        Text(
            "val graph = buildCommitGraph(commits)",
            style = NitpickTheme.typography.code,
            color = NitpickTheme.colors.text,
        )
    }
}

@Composable
private fun ControlsSection() {
    var query by remember { mutableStateOf("") }
    NitpickCard(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        NitpickSectionHeader(title = "Controls", kicker = "28dp density")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            NitpickTooltip(text = "Review this branch against main", shortcut = "⌘⇧R") {
                NitpickButton(text = "Nitpick branch", onClick = {})
            }
            NitpickButton(text = "Cancel", onClick = {}, variant = NitpickButtonVariant.Secondary)
            NitpickButton(text = "Dismiss", onClick = {}, variant = NitpickButtonVariant.Ghost)
            NitpickButton(text = "Delete", onClick = {}, variant = NitpickButtonVariant.Danger, size = NitpickButtonSize.Small)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            NitpickBadge("ready", tone = NitpickTone.Success)
            NitpickBadge("modified", tone = NitpickTone.Warning)
            NitpickBadge("bug", tone = NitpickTone.Danger)
            NitpickBadge("AI", tone = NitpickTone.Ai)
            NitpickKbd("⌘K")
        }
        NitpickTextField(
            value = query,
            onValueChange = { query = it },
            label = "Search commits",
            placeholder = "Message, author, hash…",
            modifier = Modifier.width(360.dp),
        )
        var theme by remember { mutableStateOf("System") }
        NitpickSegmentedControl(listOf("System", "Light", "Dark"), theme, { it }, { theme = it }, Modifier.width(300.dp))
        var provider by remember { mutableStateOf("Anthropic") }
        NitpickRadioGroup(listOf("Anthropic", "OpenAI", "Google", "Ollama"), provider, { it }, { provider = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NitpickTab("Details", selected = true, onClick = {})
            NitpickTab("Diff", selected = false, onClick = {})
            NitpickTab("Review", selected = false, onClick = {})
        }
        NitpickReviewLoader()
    }
}

@Composable
private fun ReviewSection() {
    NitpickCard(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NitpickSectionHeader(title = "Review", kicker = "Domain components")
        ReviewCommentCard(
            filePath = "shared/src/commonMain/.../CommitGraph.kt",
            line = 128,
            severity = Severity.BUG,
            message = "The lane can be released twice when two children converge in the same row.",
            suggestion = "clearIncomingLanes(lanes, incoming, keep = lane)",
        )
        Column(Modifier.fillMaxWidth().background(NitpickTheme.colors.editor)) {
            DiffLineRow(
                DiffLine(
                    type = DiffLineType.CONTEXT,
                    content = "@Composable",
                    oldLineNumber = 42,
                    newLineNumber = 42,
                ),
            )
            DiffLineRow(
                DiffLine(
                    type = DiffLineType.ADDED,
                    content = "val lane = expected.first()",
                    oldLineNumber = null,
                    newLineNumber = 43,
                ),
            )
            DiffLineRow(
                DiffLine(
                    type = DiffLineType.REMOVED,
                    content = "val lane = expected.last()",
                    oldLineNumber = 44,
                    newLineNumber = null,
                ),
            )
        }
        NitSheetContent(
            comment = ReviewComment(
                id = "nit-1",
                filePath = "review/NitAnchor.kt",
                startLine = 23,
                endLine = 23,
                severity = Severity.WARNING,
                category = "logic",
                message = "The snippet is compared before it is trimmed, so trailing spaces break the anchor.",
                originalSnippet = "if (snippet == lines[i])",
                suggestion = "if (snippet.trim() == lines[i].trim())",
            ),
            position = 1,
            total = 8,
            onApply = {},
            onDismiss = {},
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RefBadge(Ref("main", "refs/heads/main", "abc123", RefType.LOCAL_BRANCH))
            RefBadge(Ref("origin/main", "refs/remotes/origin/main", "abc123", RefType.REMOTE_BRANCH))
            RefBadge(Ref("v1.0", "refs/tags/v1.0", "abc123", RefType.TAG))
        }
    }
}

@Composable
private fun ShellSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NitpickSectionHeader(title = "IDE shell", kicker = "Toolbar · tool window · status bar")
        NitpickToolbar {
            Text("Nitpick", style = NitpickTheme.typography.bodyStrong, color = NitpickTheme.colors.onToolbar, fontWeight = FontWeight.SemiBold)
            Text("/ nitpick-core", style = NitpickTheme.typography.body, color = NitpickTheme.colors.onToolbar.copy(alpha = 0.72f))
            Spacer(Modifier.weight(1f))
            NitpickToolbarAction("Open repository…", onClick = {}, selected = false)
        }
        NitpickToolWindowHeader("Commits") {
            Text("main", style = NitpickTheme.typography.micro, color = NitpickTheme.colors.onPanelHeader)
        }
        NitpickStatusBar {
            Text("main", style = NitpickTheme.typography.micro, color = NitpickTheme.colors.onStatusBar)
            Text("500 commits", style = NitpickTheme.typography.micro, color = NitpickTheme.colors.onStatusBar)
            Spacer(Modifier.weight(1f))
            Text("Git", style = NitpickTheme.typography.micro, color = NitpickTheme.colors.onStatusBar)
        }
    }
}

@Preview
@Composable
private fun DesignSystemCatalogLightPreview() {
    DesignSystemCatalog(darkTheme = false)
}

@Preview
@Composable
private fun DesignSystemCatalogDarkPreview() {
    DesignSystemCatalog(darkTheme = true)
}
