package dev.nitpick.app.ui.phone

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import dev.nitpick.app.ui.components.NitpickIcon
import nitpick.shared.generated.resources.ic_gear
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.Credentials
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.SharedReview
import dev.nitpick.app.settings.ForgeClient
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

private val PhoneIconActionSize = 44.dp
private const val ScreenTransitionMillis = 220
private val PhoneActionHeight = 56.dp

private enum class PhoneScreen { Repos, Clone, Graph, Review, Diff, Shared }

private val PhoneScreen.parent: PhoneScreen
    get() = when (this) {
        PhoneScreen.Repos -> PhoneScreen.Repos
        PhoneScreen.Clone, PhoneScreen.Graph -> PhoneScreen.Repos
        PhoneScreen.Review, PhoneScreen.Shared -> PhoneScreen.Graph
        PhoneScreen.Diff -> PhoneScreen.Review
    }

/**
 * Compact layout (CONTEXT §12): one screen at a time, Repos → Clone → Graph → Review → Diff.
 * Graph also opens Shared, the reviews another device shared for this repository.
 * The phone needs a forge account first: every clone and push uses [Settings.gitConnection].
 * [cloneRoot] is the folder clones land in.
 */
@OptIn(ExperimentalComposeUiApi::class)
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
    onPublish: (Credentials?) -> Unit,
    onPull: (Credentials?) -> Unit,
    onShareReview: () -> Unit,
    onLoadSharedReviews: () -> Unit,
    onOpenSharedReview: (SharedReview) -> Unit,
    onFetch: (Credentials?) -> Unit,
    onConnect: (GitConnection) -> Unit,
    forge: ForgeClient,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var screen by rememberSaveable { mutableStateOf(if (state.path != null) PhoneScreen.Graph else PhoneScreen.Repos) }
    var editingConnection by rememberSaveable { mutableStateOf(false) }
    // System Back follows the same parent as each screen's in-app back button.
    BackHandler(enabled = screen != PhoneScreen.Repos) { screen = screen.parent }
    val connection = settings.gitConnection?.takeIf { it.isComplete }
    if (connection == null || editingConnection) {
        ConnectScreen(
            current = connection,
            forge = forge,
            onConnect = {
                onConnect(it)
                editingConnection = false
            },
            onCancel = if (connection != null) ({ editingConnection = false }) else null,
            modifier = modifier,
        )
        return
    }
    // Deeper screens slide in from the right, going back slides from the left, like a native stack.
    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            val forward = initialState.parent != targetState
            val enter = slideInHorizontally(tween(ScreenTransitionMillis)) { if (forward) it else -it } + fadeIn(tween(ScreenTransitionMillis))
            val exit = slideOutHorizontally(tween(ScreenTransitionMillis)) { if (forward) -it / 3 else it / 3 } + fadeOut(tween(ScreenTransitionMillis))
            enter togetherWith exit
        },
        label = "phone screen",
    ) { shown ->
    when (shown) {
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
            accountLogin = connection.login.takeIf { it.isNotBlank() }?.let { "@$it" }.orEmpty(),
            accountIcon = connection.host.icon,
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
            onPublish = { onPublish(connection.toCredentials()) },
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
}

@Composable
internal fun PhoneTopBar(
    title: String,
    onBack: (() -> Unit)?,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    NitpickToolbar {
        if (onBack != null) {
            NitpickToolbarAction(text = "‹ " + stringResource(Res.string.action_back), onClick = onBack)
        }
        Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
            Text(
                text = title,
                style = NitpickTheme.typography.bodyStrong,
                color = NitpickTheme.colors.onToolbar,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = NitpickTheme.typography.micro,
                    color = NitpickTheme.colors.onToolbar.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        actions()
    }
}

/** An icon in the top bar with a 44 dp touch target; the description is what TalkBack reads. */
@Composable
internal fun PhoneIconAction(resource: DrawableResource, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(PhoneIconActionSize).clip(RoundedCornerShape(NitpickTheme.radii.xs)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NitpickIcon(resource, contentDescription, tint = NitpickTheme.colors.onToolbar)
    }
}

@Composable
internal fun SettingsAction(onOpenSettings: () -> Unit) {
    PhoneIconAction(Res.drawable.ic_gear, stringResource(Res.string.settings_title), onOpenSettings)
}

/** The single full-width action at the bottom of a phone screen (UI handoff §4). */
@Composable
internal fun PhoneBottomButton(text: String, onClick: () -> Unit, enabled: Boolean = true, icon: DrawableResource? = null) {
    NitpickButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        size = NitpickButtonSize.Large,
        modifier = Modifier.fillMaxWidth().padding(16.dp).height(PhoneActionHeight),
        leading = icon?.let { { NitpickIcon(it, contentDescription = null, tint = NitpickTheme.colors.onPrimary) } },
    )
}
