package dev.nitpick.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.nitpick.app.AppContainer
import dev.nitpick.app.ui.repository.RepositoryScreen
import dev.nitpick.app.ui.repository.RepositoryViewModel

/**
 * Shared entry point of the UI. [pickRepositoryPath] opens the platform folder picker and
 * returns the chosen path, or null if the user cancels; platforms without one pass null.
 */
@Composable
fun App(container: AppContainer, pickRepositoryPath: (() -> String?)? = null) {
    val viewModel = viewModel { RepositoryViewModel(container.gitRepositoryFactory) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val openRepository: (() -> Unit)? = pickRepositoryPath?.let { pick ->
        { pick()?.let(viewModel::openRepository) }
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            RepositoryScreen(state, openRepository, viewModel::selectCommit, Modifier.safeContentPadding())
        }
    }
}
