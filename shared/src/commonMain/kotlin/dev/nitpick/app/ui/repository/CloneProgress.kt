package dev.nitpick.app.ui.repository

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** What a clone form shows of the clone it started with [start]. */
internal class CloneProgress(val isCloning: Boolean, val errorMessage: String?, val start: (path: String) -> Unit)

/** Tracks the clone a form started; [onCloned] fires once that repository is loaded. */
@Composable
internal fun rememberCloneProgress(state: RepositoryUiState, onCloned: () -> Unit): CloneProgress {
    var targetPath by rememberSaveable { mutableStateOf<String?>(null) }
    val clone = state.clone?.takeIf { it.path == targetPath }
    // The clone is done and its repository replaced the state; loading the log may still fail.
    val isLoaded = targetPath != null && state.clone == null && state.path == targetPath
    val isCloning = (clone != null && clone.errorMessage == null) || (isLoaded && state.isLoading)
    val errorMessage = clone?.errorMessage ?: state.errorMessage.takeIf { isLoaded }

    LaunchedEffect(isLoaded, state.isLoading, state.errorMessage) {
        if (isLoaded && !state.isLoading && state.errorMessage == null) onCloned()
    }

    return CloneProgress(isCloning, errorMessage, start = { targetPath = it })
}

/** `https://github.com/acme/demo.git` → `demo`, like `git clone` does. */
internal fun defaultCloneFolderName(url: String): String =
    url.trim().trimEnd('/').substringAfterLast('/').substringAfterLast(':').removeSuffix(".git")

/** The clone must stay inside the clone root, so the name cannot be a path. */
internal fun isValidCloneFolderName(name: String): Boolean =
    name.isNotBlank() && name != "." && name != ".." && name.none { it == '/' || it == '\\' }
