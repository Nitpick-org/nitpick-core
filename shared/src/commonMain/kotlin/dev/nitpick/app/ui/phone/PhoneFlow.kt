package dev.nitpick.app.ui.phone

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.Credentials
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.SharedReview
import dev.nitpick.app.settings.DeviceFlowClient
import dev.nitpick.app.settings.GitConnection
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickToolbar
import dev.nitpick.app.ui.components.NitpickToolbarAction
import dev.nitpick.app.ui.repository.RepositoryUiState
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.action_back
import nitpick.shared.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource

private val PhoneActionHeight = 56.dp

private enum class PhoneScreen { Repos, Clone, Graph, Review, Diff, Shared }

/**
 * Compact layout (CONTEXT §12): one screen at a time, Repos → Clone → Graph → Review → Diff.
 * Graph also opens Shared, the reviews another device shared for this repository.
 * The phone needs a forge account first: every clone and push uses [Settings.gitConnection].
 * [cloneRoot] is the folder clones land in.
 */
@Composable
fun PhoneFlow(
    state: RepositoryUiState,
    settings: Settings,
    cloneRoot: String,
    onOpenRepository: (String) -> Unit,
    onCloneRepository: (url: String, path: String, credentials: Credentials?) -> Unit,
    onSelectCommit: (String) -> Unit,
    onCheckoutBranch: (String) -> Unit,
    onRunReview: () -> Unit,
    onSelectDiffFile: (String) -> Unit,
    onApplySuggestion: (ReviewComment) -> Unit,
    onDismissSuggestion: (String) -> Unit,
    onCommitMessage: (String) -> Unit,
    onCommit: () -> Unit,
    onPush: (Credentials?) -> Unit,
    onPull: (Credentials?) -> Unit,
    onShareReview: () -> Unit,
    onLoadSharedReviews: () -> Unit,
    onOpenSharedReview: (SharedReview) -> Unit,
    onFetch: (Credentials?) -> Unit,
    onConnect: (GitConnection) -> Unit,
    deviceFlow: DeviceFlowClient,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var screen by rememberSaveable { mutableStateOf(if (state.path != null) PhoneScreen.Graph else PhoneScreen.Repos) }
    var editingConnection by rememberSaveable { mutableStateOf(false) }
    val connection = settings.gitConnection?.takeIf { it.isComplete }
    if (connection == null || editingConnection) {
        ConnectScreen(
            current = connection,
            deviceFlow = deviceFlow,
            onConnect = {
                onConnect(it)
                editingConnection = false
            },
            onCancel = if (connection != null) ({ editingConnection = false }) else null,
            modifier = modifier,
        )
        return
    }
    when (screen) {
        PhoneScreen.Repos -> ReposScreen(
            recentRepositories = settings.recentRepositories,
            onOpen = { path ->
                onOpenRepository(path)
                screen = PhoneScreen.Graph
            },
            onClone = { screen = PhoneScreen.Clone },
            onEditConnection = { editingConnection = true },
            onOpenSettings = onOpenSettings,
            modifier = modifier,
        )
        PhoneScreen.Clone -> CloneScreen(
            state = state,
            cloneRoot = cloneRoot,
            onClone = { url, path -> onCloneRepository(url, path, connection.toCredentials()) },
            onCancel = { screen = PhoneScreen.Repos },
            onCloned = { screen = PhoneScreen.Graph },
            modifier = modifier,
        )
        PhoneScreen.Graph -> PhoneGraphScreen(
            state = state,
            onBack = { screen = PhoneScreen.Repos },
            onSelectCommit = onSelectCommit,
            onCheckoutBranch = onCheckoutBranch,
            onPull = { onPull(connection.toCredentials()) },
            onPush = { onPush(connection.toCredentials()) },
            onFetch = { onFetch(connection.toCredentials()) },
            onReview = {
                if (state.review == null && !state.isReviewing) onRunReview()
                screen = PhoneScreen.Review
            },
            onOpenShared = { screen = PhoneScreen.Shared },
            onOpenSettings = onOpenSettings,
            modifier = modifier,
        )
        PhoneScreen.Review -> PhoneReviewScreen(
            state = state,
            onBack = { screen = PhoneScreen.Graph },
            onRunReview = onRunReview,
            onOpenFile = { path ->
                onSelectDiffFile(path)
                screen = PhoneScreen.Diff
            },
            onApply = onApplySuggestion,
            onDismiss = onDismissSuggestion,
            onCommitMessage = onCommitMessage,
            onCommit = onCommit,
            onPush = { onPush(connection.toCredentials()) },
            onShare = onShareReview,
            onOpenSettings = onOpenSettings,
            modifier = modifier,
        )
        PhoneScreen.Diff -> PhoneDiffScreen(
            // No fallback to the first file: a nit anchored outside the diff shows the empty state instead.
            file = state.diff.firstOrNull { it.path == state.selectedDiffPath },
            baseRef = state.baseRef,
            onBack = { screen = PhoneScreen.Review },
            modifier = modifier,
        )
        PhoneScreen.Shared -> PhoneSharedReviewsScreen(
            state = state,
            onBack = { screen = PhoneScreen.Graph },
            onRefresh = onLoadSharedReviews,
            onOpen = { shared ->
                onOpenSharedReview(shared)
                screen = PhoneScreen.Review
            },
            modifier = modifier,
        )
    }
}

@Composable
internal fun PhoneTopBar(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit = {},
) {
    NitpickToolbar {
        if (onBack != null) {
            NitpickToolbarAction(text = "‹ " + stringResource(Res.string.action_back), onClick = onBack)
        }
        Text(
            text = title,
            style = NitpickTheme.typography.bodyStrong,
            color = NitpickTheme.colors.onToolbar,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
        )
        actions()
    }
}

@Composable
internal fun SettingsAction(onOpenSettings: () -> Unit) {
    NitpickToolbarAction(text = stringResource(Res.string.settings_title), onClick = onOpenSettings)
}

/** The single full-width action at the bottom of a phone screen (UI handoff §4). */
@Composable
internal fun PhoneBottomButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    NitpickButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        size = NitpickButtonSize.Large,
        modifier = Modifier.fillMaxWidth().padding(16.dp).height(PhoneActionHeight),
    )
}
