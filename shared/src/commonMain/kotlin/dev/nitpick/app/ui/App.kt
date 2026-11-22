package dev.nitpick.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.nitpick.app.AppContainer
import dev.nitpick.app.settings.DeviceFlowException
import dev.nitpick.app.settings.withRecentRepository
import dev.nitpick.app.ui.phone.PhoneFlow
import dev.nitpick.app.ui.repository.CloneDialog
import dev.nitpick.app.ui.repository.RepositoryScreen
import dev.nitpick.app.ui.repository.RepositoryViewModel
import dev.nitpick.app.ui.settings.SettingsDialog
import dev.nitpick.app.ui.settings.SettingsScreen
import dev.nitpick.app.ui.settings.SettingsViewModel
import dev.nitpick.app.ui.theme.NitpickTheme

/**
 * Shared entry point of the UI. [pickRepositoryPath] opens the platform folder picker and
 * returns the chosen path, or null if the user cancels; platforms without one pass null.
 * [cloneRoot] is where HTTPS clones are stored: a platform that has one (Android) gets the
 * phone flow, one screen at a time, whatever the window size.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(
    container: AppContainer,
    pickRepositoryPath: (() -> String?)? = null,
    cloneRoot: String? = null,
) {
    val repositoryViewModel = viewModel { RepositoryViewModel(
            container.gitRepositoryFactory,
            reviewAgent = container.reviewAgent,
            shareClient = container.reviewShareClient,
        ) }
    val settingsViewModel = viewModel { SettingsViewModel(container.settingsStore) }
    val state by repositoryViewModel.state.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val settingsLoaded by settingsViewModel.isLoaded.collectAsStateWithLifecycle()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showClone by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showSettings) { showSettings = false }
    val openRepository: (() -> Unit)? = pickRepositoryPath?.let { pick ->
        { pick()?.let(repositoryViewModel::openRepository) }
    }

    // A connection saved before the login field existed gets its account name once, so the
    // Repos bar shows who is signed in without a new sign-in. A failure just leaves it blank.
    LaunchedEffect(settingsLoaded, settings.gitConnection) {
        val connection = settings.gitConnection ?: return@LaunchedEffect
        if (!settingsLoaded || !connection.isComplete || connection.login.isNotBlank()) return@LaunchedEffect
        val login = try {
            container.forgeClient.fetchLogin(connection.host, connection.token)
        } catch (e: DeviceFlowException) {
            return@LaunchedEffect
        }
        settingsViewModel.update { current ->
            if (current.gitConnection == connection) current.copy(gitConnection = connection.copy(login = login)) else current
        }
    }

    // An open repository goes to the top of the recent list, which the phone Repos screen shows.
    LaunchedEffect(state.path, state.isLoading, state.errorMessage) {
        val path = state.path
        if (path != null && !state.isLoading && state.errorMessage == null) {
            settingsViewModel.update { it.withRecentRepository(path) }
        }
    }

    // key(language): every stringResource is re-read when the language changes.
    CompositionLocalProvider(LocalAppLocale provides settings.language.tag) {
        key(settings.language) {
            NitpickTheme(settings.themeMode) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when {
                        showSettings && cloneRoot != null -> SettingsScreen(settings, settingsViewModel::update, onBack = { showSettings = false })
                        // The account gate must not flash before the settings file is read.
                        cloneRoot != null && !settingsLoaded -> Unit
                        cloneRoot != null -> PhoneFlow(
                            state = state,
                            settings = settings,
                            cloneRoot = cloneRoot,
                            onOpenRepository = repositoryViewModel::openRepository,
                            onCloneRepository = repositoryViewModel::cloneRepository,
                            onSelectCommit = repositoryViewModel::selectCommit,
                            onCheckoutBranch = repositoryViewModel::checkoutBranch,
                            onRunReview = repositoryViewModel::reviewBranch,
                            onSelectDiffFile = repositoryViewModel::selectDiffFile,
                            onApplySuggestion = repositoryViewModel::applySuggestion,
                            onDismissSuggestion = repositoryViewModel::dismissSuggestion,
                            onCommitMessage = repositoryViewModel::setCommitMessage,
                            onCommit = repositoryViewModel::commitAppliedSuggestions,
                            onPush = repositoryViewModel::pushBranch,
                            onPublish = repositoryViewModel::publishBranch,
                            onPull = repositoryViewModel::pullBranch,
                            onShareReview = repositoryViewModel::shareReview,
                            onLoadSharedReviews = repositoryViewModel::loadSharedReviews,
                            onOpenSharedReview = repositoryViewModel::openSharedReview,
                            onFetch = repositoryViewModel::fetchBranch,
                            onConnect = { connection -> settingsViewModel.update { it.copy(gitConnection = connection) } },
                            forge = container.forgeClient,
                            onOpenSettings = { showSettings = true },
                            modifier = Modifier.safeContentPadding(),
                        )
                        else -> {
                            RepositoryScreen(
                            state = state,
                            settings = settings,
                            onOpenRepository = openRepository,
                            onCloneRepository = pickRepositoryPath?.let { { showClone = true } },
                            onSelectCommit = repositoryViewModel::selectCommit,
                            onShowDiff = repositoryViewModel::refreshDiff,
                            onCheckoutBranch = repositoryViewModel::checkoutBranch,
                            onPull = { repositoryViewModel.pullBranch(settings.gitConnection?.takeIf { it.isComplete }?.toCredentials()) },
                            onPush = { repositoryViewModel.pushBranch(settings.gitConnection?.takeIf { it.isComplete }?.toCredentials()) },
                            onPublish = { repositoryViewModel.publishBranch(settings.gitConnection?.takeIf { it.isComplete }?.toCredentials()) },
                            onFetch = { repositoryViewModel.fetchBranch(settings.gitConnection?.takeIf { it.isComplete }?.toCredentials()) },
                            onSelectBase = repositoryViewModel::selectBase,
                            onSelectDiffFile = repositoryViewModel::selectDiffFile,
                            onOpenSettings = { showSettings = true },
                            onRunReview = repositoryViewModel::reviewBranch,
                            onApplySuggestion = repositoryViewModel::applySuggestion,
                            onApplyAll = repositoryViewModel::applyAllSuggestions,
                            onDismissSuggestion = repositoryViewModel::dismissSuggestion,
                            onCommitMessage = repositoryViewModel::setCommitMessage,
                            onCommit = repositoryViewModel::commitAppliedSuggestions,
                            onShareReview = repositoryViewModel::shareReview,
                            onLoadSharedReviews = repositoryViewModel::loadSharedReviews,
                            onOpenSharedReview = repositoryViewModel::openSharedReview,
                            modifier = Modifier.safeContentPadding(),
                        )
                        if (showClone && pickRepositoryPath != null) {
                            CloneDialog(
                                state = state,
                                pickDirectory = pickRepositoryPath,
                                onClone = repositoryViewModel::cloneRepository,
                                onCancel = { showClone = false },
                                onCloned = { showClone = false },
                            )
                        }
                        if (showSettings) {
                            val draft by settingsViewModel.draft.collectAsStateWithLifecycle()
                            LaunchedEffect(Unit) { settingsViewModel.startEditing() }
                            draft?.let {
                                SettingsDialog(
                                    draft = it,
                                    onEdit = settingsViewModel::editDraft,
                                    onSave = { settingsViewModel.saveDraft(); showSettings = false },
                                    onCancel = { settingsViewModel.discardDraft(); showSettings = false },
                                )
                            }
                        }
                        }
                    }
                }
            }
        }
    }
}
