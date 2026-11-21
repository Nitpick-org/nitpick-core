package dev.nitpick.app.ui.repository

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.SharedReview
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickDivider
import dev.nitpick.app.ui.components.NitpickEmptyState
import dev.nitpick.app.ui.components.NitpickIcon
import dev.nitpick.app.ui.components.NitpickStatusBar
import dev.nitpick.app.ui.components.NitpickTab
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.components.NitpickToolbar
import dev.nitpick.app.ui.components.NitpickToolbarAction
import dev.nitpick.app.ui.components.NitpickToolWindowHeader
import dev.nitpick.app.ui.components.NitpickTooltip
import dev.nitpick.app.ui.components.NitpickVerticalDivider
import dev.nitpick.app.ui.theme.NitpickTheme
import kotlin.time.Clock
import nitpick.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val InspectorWideWidth = 680.dp
private val RefSections = listOf(
    RefType.LOCAL_BRANCH to Res.string.refs_local,
    RefType.REMOTE_BRANCH to Res.string.refs_remote_branches,
    RefType.TAG to Res.string.refs_tags,
)
private const val ReviewShortcut = "⌘⇧R"
private const val FetchShortcut = "⌘⇧F"
private const val PullShortcut = "⌘⇧L"
private const val PushShortcut = "⌘⇧P"

enum class InspectorTab { Details, Diff, Review }

/**
 * Desktop shell from docs/design/UI-HANDOFF.md §3.1: branches tool window | commit graph |
 * tabbed inspector, framed by the IDE toolbar and status bar.
 *
 * [onOpenRepository] and [onCloneRepository] are null on platforms without a folder picker.
 */
@Composable
fun RepositoryScreen(
    state: RepositoryUiState,
    settings: Settings,
    onOpenRepository: (() -> Unit)?,
    onCloneRepository: (() -> Unit)?,
    onSelectCommit: (String) -> Unit,
    onShowDiff: () -> Unit,
    onCheckoutBranch: (String) -> Unit,
    onPull: () -> Unit,
    onPush: () -> Unit,
    onFetch: () -> Unit,
    onSelectBase: (String) -> Unit,
    onSelectDiffFile: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onRunReview: () -> Unit,
    onApplySuggestion: (ReviewComment) -> Unit,
    onDismissSuggestion: (String) -> Unit,
    onCommitMessage: (String) -> Unit,
    onCommit: () -> Unit,
    onShareReview: () -> Unit,
    onLoadSharedReviews: () -> Unit,
    onOpenSharedReview: (SharedReview) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableStateOf(InspectorTab.Details) }
    val hasRepository = state.path != null && state.errorMessage == null && !state.isLoading

    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        RepositoryToolbar(
            state = state,
            hasRepository = hasRepository,
            onOpenRepository = onOpenRepository,
            onCloneRepository = onCloneRepository,
            onPull = onPull,
            onPush = onPush,
            onFetch = onFetch,
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
                        ahead = state.tracking?.ahead,
                        modifier = Modifier.width(NitpickTheme.sizes.sidebar).fillMaxHeight(),
                        onSelectBranch = onCheckoutBranch,
                    )
                    NitpickVerticalDivider()
                    CommitsPanel(state, onSelectCommit, Modifier.weight(1f).fillMaxHeight())
                    NitpickVerticalDivider()
                    Inspector(
                        state = state,
                        tab = tab,
                        onSelectTab = {
                            tab = it
                            // The working tree may have changed since the last load.
                            if (it == InspectorTab.Diff) onShowDiff()
                        },
                        onSelectBase = onSelectBase,
                        onSelectDiffFile = onSelectDiffFile,
                        onRunReview = onRunReview,
                        onApplySuggestion = onApplySuggestion,
                        onDismissSuggestion = onDismissSuggestion,
                        onCommitMessage = onCommitMessage,
                        onCommit = onCommit,
                        onOpenSettings = onOpenSettings,
                        onShareReview = onShareReview,
                        onLoadSharedReviews = onLoadSharedReviews,
                        onOpenSharedReview = onOpenSharedReview,
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
    onCloneRepository: (() -> Unit)?,
    onPull: () -> Unit,
    onPush: () -> Unit,
    onFetch: () -> Unit,
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
        if (onCloneRepository != null) {
            NitpickToolbarAction(text = stringResource(Res.string.clone_open), onClick = onCloneRepository)
        }
        if (hasRepository) {
            val syncing = state.isSyncing
            NitpickTooltip(text = stringResource(Res.string.fetch_action), shortcut = FetchShortcut) {
                NitpickToolbarAction(
                    text = stringResource(if (state.isFetching) Res.string.fetch_in_progress else Res.string.fetch_action),
                    onClick = { if (!syncing) onFetch() },
                )
            }
            NitpickTooltip(text = stringResource(Res.string.pull_action), shortcut = PullShortcut) {
                NitpickToolbarAction(
                    text = when {
                        state.isPulling -> stringResource(Res.string.pull_in_progress)
                        (state.tracking?.behind ?: 0) > 0 -> stringResource(Res.string.sync_pull_count, state.tracking!!.behind)
                        else -> stringResource(Res.string.pull_action)
                    },
                    onClick = { if (!syncing) onPull() },
                )
            }
            if (state.tracking != null) {
                NitpickTooltip(text = stringResource(Res.string.push_action), shortcut = PushShortcut) {
                    NitpickToolbarAction(
                        text = when {
                            state.isPushing -> stringResource(Res.string.push_in_progress)
                            state.tracking.ahead > 0 -> stringResource(Res.string.sync_push_count, state.tracking.ahead)
                            else -> stringResource(Res.string.push_action)
                        },
                        onClick = { if (!syncing) onPush() },
                    )
                }
            }
            NitpickVerticalDivider(Modifier.padding(horizontal = 4.dp))
            NitpickTooltip(text = stringResource(Res.string.review_cta_tooltip), shortcut = ReviewShortcut) {
                NitpickButton(
                    text = stringResource(Res.string.review_cta),
                    onClick = onReview,
                    size = NitpickButtonSize.Small,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    leading = {
                        NitpickIcon(Res.drawable.ic_tweezers, null, tint = NitpickTheme.colors.onPrimary)
                    },
                )
            }
        }
        NitpickToolbarAction(
            text = stringResource(Res.string.settings_title),
            onClick = onOpenSettings,
            leading = { NitpickIcon(Res.drawable.ic_gear, null, tint = NitpickTheme.colors.onToolbar) },
        )
    }
}

@Composable
internal fun RefsSidebar(refs: List<Ref>, currentBranch: String, modifier: Modifier, ahead: Int? = null, onSelectBranch: ((String) -> Unit)? = null) {
    var filter by rememberSaveable { mutableStateOf("") }
    var collapsed by rememberSaveable { mutableStateOf(emptySet<RefType>()) }
    val visibleRefs = remember(refs, filter) { refs.filter { it.name.contains(filter, ignoreCase = true) } }
    val sections = RefSections

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
                    val select = onSelectBranch?.takeIf { ref.type != RefType.TAG && !isCurrent }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isCurrent) NitpickTheme.colors.selection else Color.Transparent)
                            .then(if (select != null) Modifier.clickable { select(ref.name.removePrefix("origin/")) } else Modifier)
                            .padding(start = 24.dp, end = 12.dp, top = 3.dp, bottom = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        NitpickIcon(
                            resource = when (ref.type) {
                                RefType.LOCAL_BRANCH -> Res.drawable.ic_branch
                                RefType.REMOTE_BRANCH -> Res.drawable.ic_cloud
                                RefType.TAG -> Res.drawable.ic_tag
                            },
                            contentDescription = null,
                            tint = if (isCurrent) NitpickTheme.colors.onSelection else NitpickTheme.colors.textMuted,
                        )
                        Text(
                            text = ref.name + if (isCurrent && ahead != null && ahead > 0) " " + stringResource(Res.string.sync_ahead, ahead) else "",
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
}

@Composable
private fun RefSectionTitle(text: String, collapsed: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        NitpickIcon(
            resource = Res.drawable.ic_chevron_down,
            contentDescription = null,
            modifier = Modifier.rotate(if (collapsed) -90f else 0f),
            tint = NitpickTheme.colors.textMuted,
        )
        Text(text = text, style = NitpickTheme.typography.bodyStrong, color = NitpickTheme.colors.text)
    }
}

@Composable
private fun CommitsPanel(state: RepositoryUiState, onSelectCommit: (String) -> Unit, modifier: Modifier) {
    var query by rememberSaveable { mutableStateOf("") }
    var branch by rememberSaveable { mutableStateOf<String?>(null) }
    var author by rememberSaveable { mutableStateOf<String?>(null) }
    var date by rememberSaveable { mutableStateOf(CommitDateFilter.ALL) }
    val filters = CommitFilters(query = query, branch = branch, author = author, date = date)
    val authors = remember(state.graph) { state.graph.map { it.commit.authorName }.distinct().sorted() }
    val branches = remember(state.refs) {
        state.refs.filter { it.type != RefType.TAG }.map { it.name }.distinct().sorted()
    }
    // Read the clock when the date filter changes, so a long session does not keep a stale "today".
    val now = remember(date) { Clock.System.now().epochSeconds }
    val filtered = remember(state.graph, state.refs, filters, now) {
        filterCommits(state.graph, state.refs, filters, now)
    }

    Column(modifier) {
        NitpickToolWindowHeader(stringResource(Res.string.commits_title)) {
            Text(
                text = if (filters.isActive) "${filtered.size}/${state.graph.size}" else stringResource(Res.string.status_commits, state.graph.size),
                style = NitpickTheme.typography.micro,
                color = NitpickTheme.colors.onPanelHeader,
            )
        }
        if (state.graph.isEmpty()) {
            PanelHint(stringResource(Res.string.commits_empty))
        } else {
            CommitFilterBar(
                query = query,
                onQueryChange = { query = it },
                branch = branch,
                branches = branches,
                onBranchChange = { branch = it },
                author = author,
                authors = authors,
                onAuthorChange = { author = it },
                date = date,
                onDateChange = { date = it },
                active = filters.isActive,
                onClear = {
                    query = ""
                    branch = null
                    author = null
                    date = CommitDateFilter.ALL
                },
            )
            if (filters.isActive) {
                if (filtered.isEmpty()) {
                    PanelHint(stringResource(Res.string.commits_no_match))
                } else {
                    CommitGraphList(
                        graph = filtered,
                        selectedCommitId = state.selectedCommitId,
                        onSelectCommit = onSelectCommit,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        showLanes = false,
                    )
                }
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
}

@Composable
private fun CommitFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    branch: String?,
    branches: List<String>,
    onBranchChange: (String?) -> Unit,
    author: String?,
    authors: List<String>,
    onAuthorChange: (String?) -> Unit,
    date: CommitDateFilter,
    onDateChange: (CommitDateFilter) -> Unit,
    active: Boolean,
    onClear: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        NitpickTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(Res.string.commits_filter_placeholder),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            StringFilterDropdown(
                label = stringResource(Res.string.filter_branch),
                options = branches,
                selected = branch,
                onSelect = onBranchChange,
            )
            StringFilterDropdown(
                label = stringResource(Res.string.filter_user),
                options = authors,
                selected = author,
                onSelect = onAuthorChange,
            )
            DateFilterDropdown(date, onDateChange)
            if (active) {
                Text(
                    text = stringResource(Res.string.filter_clear),
                    modifier = Modifier.clickable(onClick = onClear).padding(horizontal = 6.dp, vertical = 4.dp),
                    style = NitpickTheme.typography.micro,
                    color = NitpickTheme.colors.primary,
                )
            }
        }
        if (active) {
            Text(
                text = stringResource(Res.string.filter_graph_hidden),
                style = NitpickTheme.typography.micro,
                color = NitpickTheme.colors.textMuted,
            )
        }
    }
}

@Composable
private fun StringFilterDropdown(label: String, options: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(text = "$label: ${selected ?: stringResource(Res.string.filter_all)}", onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(stringResource(Res.string.filter_all)) }, onClick = { onSelect(null); expanded = false })
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@Composable
private fun DateFilterDropdown(selected: CommitDateFilter, onSelect: (CommitDateFilter) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (selected) {
        CommitDateFilter.ALL -> stringResource(Res.string.filter_all)
        CommitDateFilter.TODAY -> stringResource(Res.string.filter_today)
        CommitDateFilter.WEEK -> stringResource(Res.string.filter_week)
        CommitDateFilter.MONTH -> stringResource(Res.string.filter_month)
    }
    Box {
        FilterChip(text = "${stringResource(Res.string.filter_date)}: $label", onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CommitDateFilter.entries.forEach { option ->
                val optionLabel = when (option) {
                    CommitDateFilter.ALL -> stringResource(Res.string.filter_all)
                    CommitDateFilter.TODAY -> stringResource(Res.string.filter_today)
                    CommitDateFilter.WEEK -> stringResource(Res.string.filter_week)
                    CommitDateFilter.MONTH -> stringResource(Res.string.filter_month)
                }
                DropdownMenuItem(text = { Text(optionLabel) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@Composable
private fun FilterChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(NitpickTheme.radii.sm),
        color = NitpickTheme.colors.surface,
        contentColor = NitpickTheme.colors.textSecondary,
        border = BorderStroke(1.dp, NitpickTheme.colors.border),
        tonalElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            style = NitpickTheme.typography.micro,
            color = NitpickTheme.colors.textSecondary,
            maxLines = 1,
        )
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
    onOpenSettings: () -> Unit,
    onSelectBase: (String) -> Unit,
    onSelectDiffFile: (String) -> Unit,
    onShareReview: () -> Unit,
    onLoadSharedReviews: () -> Unit,
    onOpenSharedReview: (SharedReview) -> Unit,
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
            InspectorTab.Diff -> DiffPanel(state, onSelectBase, onSelectDiffFile, Modifier.weight(1f))
            InspectorTab.Review -> ReviewPanel(
                state = state,
                onReview = onRunReview,
                onApply = onApplySuggestion,
                onDismiss = onDismissSuggestion,
                onCommitMessage = onCommitMessage,
                onCommit = onCommit,
                onOpenSettings = onOpenSettings,
                onShare = onShareReview,
                onLoadSharedReviews = onLoadSharedReviews,
                onOpenSharedReview = onOpenSharedReview,
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
        state.tracking?.let { tracking -> StatusText(stringResource(Res.string.sync_ahead_behind, tracking.ahead, tracking.behind)) }
        syncStatusMessage(state)?.let { message ->
            Text(
                text = message,
                style = NitpickTheme.typography.micro,
                color = if (state.hasSyncProblem()) NitpickTheme.colors.danger else NitpickTheme.colors.onStatusBar,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.weight(1f))
        if (hasRepository) StatusText("${stringResource(settings.provider.label)} · ${settings.model}")
        StatusText(stringResource(Res.string.status_git))
    }
}

@Composable
private fun StatusText(text: String) {
    Text(text = text, style = NitpickTheme.typography.micro, color = NitpickTheme.colors.onStatusBar, maxLines = 1)
}

internal val dev.nitpick.app.settings.LlmProvider.label: StringResource
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
internal fun branchLabel(currentBranch: String): String =
    if (currentBranch.length == 40 && currentBranch.all { it in "0123456789abcdef" }) {
        currentBranch.take(7)
    } else {
        currentBranch
    }

internal fun folderName(path: String): String =
    path.trimEnd('/', '\\').substringAfterLast('/').substringAfterLast('\\')
