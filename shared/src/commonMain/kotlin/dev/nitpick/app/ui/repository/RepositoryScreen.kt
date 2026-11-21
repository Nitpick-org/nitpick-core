package dev.nitpick.app.ui.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickDivider
import dev.nitpick.app.ui.components.NitpickEmptyState
import dev.nitpick.app.ui.components.NitpickStatusBar
import dev.nitpick.app.ui.components.NitpickTab
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.components.NitpickToolbar
import dev.nitpick.app.ui.components.NitpickToolbarAction
import dev.nitpick.app.ui.components.NitpickToolWindowHeader
import dev.nitpick.app.ui.components.NitpickTooltip
import dev.nitpick.app.ui.components.NitpickVerticalDivider
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val InspectorWideWidth = 680.dp
private const val ReviewShortcut = "⌘⇧R"

enum class InspectorTab { Details, Diff, Review }

/**
 * Desktop shell from docs/design/UI-HANDOFF.md §3.1: branches tool window | commit graph |
 * tabbed inspector, framed by the IDE toolbar and status bar. The diff and review tabs are
 * filled by the next steps of the handoff.
 *
 * [onOpenRepository] is null on platforms without a folder picker.
 */
@Composable
fun RepositoryScreen(
    state: RepositoryUiState,
    settings: Settings,
    onOpenRepository: (() -> Unit)?,
    onSelectCommit: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onRunReview: () -> Unit,
    onApplySuggestion: (ReviewComment) -> Unit,
    onDismissSuggestion: (String) -> Unit,
    onCommitMessage: (String) -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableStateOf(InspectorTab.Details) }
    val hasRepository = state.path != null && state.errorMessage == null && !state.isLoading

    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        RepositoryToolbar(
            state = state,
            hasRepository = hasRepository,
            onOpenRepository = onOpenRepository,
            onReview = {
                tab = InspectorTab.Review
                onRunReview()
            },
            onOpenSettings = onOpenSettings,
        )
        Box(Modifier.weight(1f)) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NitpickTheme.colors.primary)
                }
                state.errorMessage != null -> NitpickEmptyState(
                    title = stringResource(Res.string.repository_error_title),
                    detail = state.errorMessage,
                    actionLabel = if (onOpenRepository != null) stringResource(Res.string.repository_open) else null,
                    onAction = onOpenRepository,
                )
                state.path == null -> NitpickEmptyState(
                    title = stringResource(Res.string.repository_empty_title),
                    detail = if (onOpenRepository != null) stringResource(Res.string.repository_empty_hint) else null,
                    actionLabel = if (onOpenRepository != null) stringResource(Res.string.repository_open) else null,
                    onAction = onOpenRepository,
                )
                else -> Row(Modifier.fillMaxSize()) {
                    RefsSidebar(
                        refs = state.refs,
                        currentBranch = state.currentBranch,
                        modifier = Modifier.width(NitpickTheme.sizes.sidebar).fillMaxHeight(),
                    )
                    NitpickVerticalDivider()
                    CommitsPanel(state, onSelectCommit, Modifier.weight(1f).fillMaxHeight())
                    NitpickVerticalDivider()
                    Inspector(
                        state = state,
                        tab = tab,
                        onSelectTab = { tab = it },
                        onRunReview = onRunReview,
                        onApplySuggestion = onApplySuggestion,
                        onDismissSuggestion = onDismissSuggestion,
                        onCommitMessage = onCommitMessage,
                        onCommit = onCommit,
                        modifier = Modifier
                            .width(if (tab == InspectorTab.Details) NitpickTheme.sizes.inspector else InspectorWideWidth)
                            .fillMaxHeight(),
                    )
                }
            }
        }
        RepositoryStatusBar(state, settings, hasRepository)
    }
}

@Composable
private fun RepositoryToolbar(
    state: RepositoryUiState,
    hasRepository: Boolean,
    onOpenRepository: (() -> Unit)?,
    onReview: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    NitpickToolbar {
        Text(
            text = "Nitpick",
            style = NitpickTheme.typography.bodyStrong,
            color = NitpickTheme.colors.onToolbar,
        )
        if (state.path != null) {
            Text(
                text = "/ ${folderName(state.path)}",
                style = NitpickTheme.typography.body,
                color = NitpickTheme.colors.onToolbar.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (state.currentBranch.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.repository_on_branch, branchLabel(state.currentBranch)),
                    style = NitpickTheme.typography.small,
                    color = NitpickTheme.colors.onToolbar.copy(alpha = 0.55f),
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (onOpenRepository != null) {
            NitpickToolbarAction(text = stringResource(Res.string.repository_open), onClick = onOpenRepository)
        }
        if (hasRepository) {
            NitpickTooltip(text = stringResource(Res.string.review_cta_tooltip), shortcut = ReviewShortcut) {
                NitpickButton(
                    text = stringResource(Res.string.review_cta),
                    onClick = onReview,
                    size = NitpickButtonSize.Small,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
        NitpickToolbarAction(text = stringResource(Res.string.settings_title), onClick = onOpenSettings)
    }
}

@Composable
private fun RefsSidebar(refs: List<Ref>, currentBranch: String, modifier: Modifier) {
    var filter by rememberSaveable { mutableStateOf("") }
    var collapsed by rememberSaveable { mutableStateOf(emptySet<RefType>()) }
    val visibleRefs = refs.filter { it.name.contains(filter, ignoreCase = true) }
    val sections: List<Pair<RefType, StringResource>> = listOf(
        RefType.LOCAL_BRANCH to Res.string.refs_local,
        RefType.REMOTE_BRANCH to Res.string.refs_remote_branches,
        RefType.TAG to Res.string.refs_tags,
    )

    Column(modifier.background(NitpickTheme.colors.surface)) {
        NitpickToolWindowHeader(stringResource(Res.string.refs_local_branches))
        NitpickTextField(
            value = filter,
            onValueChange = { filter = it },
            placeholder = stringResource(Res.string.refs_filter_placeholder),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        )
        LazyColumn(Modifier.weight(1f).padding(bottom = 4.dp)) {
            if (visibleRefs.isEmpty()) {
                item { PanelHint(stringResource(Res.string.refs_empty)) }
            }
            sections.forEach { (type, title) ->
                val refsInSection = visibleRefs.filter { it.type == type }
                if (refsInSection.isEmpty()) return@forEach
                val isCollapsed = type in collapsed
                item {
                    RefSectionTitle(
                        text = stringResource(title),
                        collapsed = isCollapsed,
                        onClick = { collapsed = if (isCollapsed) collapsed - type else collapsed + type },
                    )
                }
                if (isCollapsed) return@forEach
                items(refsInSection, key = { it.fullName }) { ref ->
                    val isCurrent = ref.type == RefType.LOCAL_BRANCH && ref.name == currentBranch
                    Text(
                        text = ref.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isCurrent) NitpickTheme.colors.selection else Color.Transparent)
                            .padding(start = 28.dp, end = 12.dp, top = 3.dp, bottom = 3.dp),
                        style = NitpickTheme.typography.body,
                        fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
                        color = if (isCurrent) NitpickTheme.colors.onSelection else NitpickTheme.colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun RefSectionTitle(text: String, collapsed: Boolean, onClick: () -> Unit) {
    Text(
        text = (if (collapsed) "▸ " else "▾ ") + text,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 4.dp),
        style = NitpickTheme.typography.bodyStrong,
        color = NitpickTheme.colors.text,
    )
}

@Composable
private fun CommitsPanel(state: RepositoryUiState, onSelectCommit: (String) -> Unit, modifier: Modifier) {
    Column(modifier) {
        NitpickToolWindowHeader(stringResource(Res.string.commits_title)) {
            Text(
                text = stringResource(Res.string.status_commits, state.graph.size),
                style = NitpickTheme.typography.micro,
                color = NitpickTheme.colors.onPanelHeader,
            )
        }
        if (state.graph.isEmpty()) {
            PanelHint(stringResource(Res.string.commits_empty))
        } else {
            CommitGraphList(
                graph = state.graph,
                selectedCommitId = state.selectedCommitId,
                onSelectCommit = onSelectCommit,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Inspector(
    state: RepositoryUiState,
    tab: InspectorTab,
    onSelectTab: (InspectorTab) -> Unit,
    onRunReview: () -> Unit,
    onApplySuggestion: (ReviewComment) -> Unit,
    onDismissSuggestion: (String) -> Unit,
    onCommitMessage: (String) -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier.background(NitpickTheme.colors.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(NitpickTheme.colors.panelHeader).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            InspectorTab.entries.forEach { candidate ->
                NitpickTab(
                    text = stringResource(candidate.label),
                    selected = candidate == tab,
                    onClick = { onSelectTab(candidate) },
                )
            }
        }
        NitpickDivider()
        when (tab) {
            InspectorTab.Details -> {
                val selected = state.selectedCommit
                if (selected != null) {
                    CommitDetails(selected, Modifier.weight(1f))
                } else {
                    NitpickEmptyState(
                        title = stringResource(Res.string.details_placeholder),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            InspectorTab.Diff -> DiffPanel(state.diff, state.baseRef, Modifier.weight(1f))
            InspectorTab.Review -> ReviewPanel(
                state = state,
                onReview = onRunReview,
                onApply = onApplySuggestion,
                onDismiss = onDismissSuggestion,
                onCommitMessage = onCommitMessage,
                onCommit = onCommit,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private val InspectorTab.label: StringResource
    get() = when (this) {
        InspectorTab.Details -> Res.string.tab_details
        InspectorTab.Diff -> Res.string.tab_diff
        InspectorTab.Review -> Res.string.tab_review
    }

@Composable
private fun RepositoryStatusBar(state: RepositoryUiState, settings: Settings, hasRepository: Boolean) {
    NitpickStatusBar {
        StatusText(state.currentBranch.ifEmpty { stringResource(Res.string.status_no_branch) })
        if (hasRepository) StatusText(stringResource(Res.string.status_commits, state.graph.size))
        Spacer(Modifier.weight(1f))
        if (hasRepository) StatusText("${stringResource(settings.provider.label)} · ${settings.model}")
        StatusText(stringResource(Res.string.status_git))
    }
}

@Composable
private fun StatusText(text: String) {
    Text(text = text, style = NitpickTheme.typography.micro, color = NitpickTheme.colors.onStatusBar, maxLines = 1)
}

private val dev.nitpick.app.settings.LlmProvider.label: StringResource
    get() = when (this) {
        dev.nitpick.app.settings.LlmProvider.ANTHROPIC -> Res.string.provider_anthropic
        dev.nitpick.app.settings.LlmProvider.OPENAI -> Res.string.provider_openai
        dev.nitpick.app.settings.LlmProvider.GOOGLE -> Res.string.provider_google
        dev.nitpick.app.settings.LlmProvider.OLLAMA -> Res.string.provider_ollama
    }

@Composable
private fun PanelHint(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        style = NitpickTheme.typography.small,
        color = NitpickTheme.colors.textMuted,
    )
}

// On a detached HEAD, currentBranch is a full commit id: shorten it like the commit list does.
private fun branchLabel(currentBranch: String): String =
    if (currentBranch.length == 40 && currentBranch.all { it in "0123456789abcdef" }) {
        currentBranch.take(7)
    } else {
        currentBranch
    }

private fun folderName(path: String): String =
    path.trimEnd('/', '\\').substringAfterLast('/').substringAfterLast('\\')
