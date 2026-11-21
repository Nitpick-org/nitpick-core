package dev.nitpick.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.nitpick.app.AppContainer
import dev.nitpick.app.settings.withRecentRepository
import dev.nitpick.app.ui.phone.PhoneFlow
import dev.nitpick.app.ui.repository.RepositoryScreen
import dev.nitpick.app.ui.repository.RepositoryViewModel
import dev.nitpick.app.ui.settings.SettingsScreen
import dev.nitpick.app.ui.settings.SettingsViewModel
import dev.nitpick.app.ui.theme.NitpickTheme

/**
 * Shared entry point of the UI. [pickRepositoryPath] opens the platform folder picker and
 * returns the chosen path, or null if the user cancels; platforms without one pass null.
 * [cloneRoot] is where HTTPS clones are stored: a platform that has one (Android) gets the
 * phone flow, one screen at a time, whatever the window size.
 */
@Composable
fun App(
    container: AppContainer,
    pickRepositoryPath: (() -> String?)? = null,
    cloneRoot: String? = null,
) {
    val repositoryViewModel = viewModel { RepositoryViewModel(container.gitRepositoryFactory, reviewAgent = container.reviewAgent) }
    val settingsViewModel = viewModel { SettingsViewModel(container.settingsStore) }
    val state by repositoryViewModel.state.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val openRepository: (() -> Unit)? = pickRepositoryPath?.let { pick ->
        { pick()?.let(repositoryViewModel::openRepository) }
    }

    // An open repository goes to the top of the recent list, which the phone Repos screen shows.
    LaunchedEffect(state.path, state.isLoading, state.errorMessage) {
        val path = state.path
        if (path != null && !state.isLoading && state.errorMessage == null) {
            settingsViewModel.update { it.withRecentRepository(path) }
        }
    }

    NitpickTheme(settings.themeMode) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when {
                showSettings -> SettingsScreen(settings, settingsViewModel::update, onBack = { showSettings = false })
                cloneRoot != null -> PhoneFlow(
                    state = state,
                    settings = settings,
                    cloneRoot = cloneRoot,
                    onOpenRepository = repositoryViewModel::openRepository,
                    onCloneRepository = repositoryViewModel::cloneRepository,
                    onSelectCommit = repositoryViewModel::selectCommit,
                    onOpenSettings = { showSettings = true },
                    modifier = Modifier.safeContentPadding(),
                )
                else -> RepositoryScreen(
                    state = state,
                    settings = settings,
                    onOpenRepository = openRepository,
                    onSelectCommit = repositoryViewModel::selectCommit,
                    onShowDiff = repositoryViewModel::refreshDiff,
                    onSelectBase = repositoryViewModel::selectBase,
                    onSelectDiffFile = repositoryViewModel::selectDiffFile,
                    onOpenSettings = { showSettings = true },
                    onRunReview = repositoryViewModel::reviewBranch,
                    onApplySuggestion = repositoryViewModel::applySuggestion,
                    onDismissSuggestion = repositoryViewModel::dismissSuggestion,
                    onCommitMessage = repositoryViewModel::setCommitMessage,
                    onCommit = repositoryViewModel::commitAppliedSuggestions,
                    modifier = Modifier.safeContentPadding(),
                )
            }
        }
    }
}
