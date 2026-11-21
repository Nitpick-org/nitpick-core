package dev.nitpick.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.nitpick.app.AppContainer
import dev.nitpick.app.ui.repository.RepositoryScreen
import dev.nitpick.app.ui.repository.RepositoryViewModel
import dev.nitpick.app.ui.settings.SettingsScreen
import dev.nitpick.app.ui.settings.SettingsViewModel
import dev.nitpick.app.ui.theme.NitpickTheme

/**
 * Shared entry point of the UI. [pickRepositoryPath] opens the platform folder picker and
 * returns the chosen path, or null if the user cancels; platforms without one pass null.
 */
@Composable
fun App(container: AppContainer, pickRepositoryPath: (() -> String?)? = null) {
    val repositoryViewModel = viewModel { RepositoryViewModel(container.gitRepositoryFactory) }
    val settingsViewModel = viewModel { SettingsViewModel(container.settingsStore) }
    val state by repositoryViewModel.state.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val openRepository: (() -> Unit)? = pickRepositoryPath?.let { pick ->
        { pick()?.let(repositoryViewModel::openRepository) }
    }

    NitpickTheme(settings.themeMode) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (showSettings) {
                SettingsScreen(settings, settingsViewModel::update, onBack = { showSettings = false })
            } else {
                RepositoryScreen(
                    state = state,
                    settings = settings,
                    onOpenRepository = openRepository,
                    onSelectCommit = repositoryViewModel::selectCommit,
                    onOpenSettings = { showSettings = true },
                    modifier = Modifier.safeContentPadding(),
                )
            }
        }
    }
}
