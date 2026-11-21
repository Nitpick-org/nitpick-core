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

private enum class PhoneScreen { Repos, Clone, Graph }

/**
 * Compact layout (CONTEXT §12): one screen at a time, Repos → Clone → Graph.
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
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var screen by rememberSaveable { mutableStateOf(if (state.path != null) PhoneScreen.Graph else PhoneScreen.Repos) }
    when (screen) {
        PhoneScreen.Repos -> ReposScreen(
            recentRepositories = settings.recentRepositories,
            onOpen = { path ->
                onOpenRepository(path)
                screen = PhoneScreen.Graph
            },
            onClone = { screen = PhoneScreen.Clone },
            onOpenSettings = onOpenSettings,
            modifier = modifier,
        )
        PhoneScreen.Clone -> CloneScreen(
            state = state,
            cloneRoot = cloneRoot,
            onClone = onCloneRepository,
            onCancel = { screen = PhoneScreen.Repos },
            onCloned = { screen = PhoneScreen.Graph },
            modifier = modifier,
        )
        PhoneScreen.Graph -> PhoneGraphScreen(
            state = state,
            onBack = { screen = PhoneScreen.Repos },
            onSelectCommit = onSelectCommit,
            onOpenSettings = onOpenSettings,
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
