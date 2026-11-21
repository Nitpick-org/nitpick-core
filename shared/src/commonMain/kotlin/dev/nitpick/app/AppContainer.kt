package dev.nitpick.app

import dev.nitpick.app.git.GitRepositoryFactory
import dev.nitpick.app.settings.SettingsStore

/**
 * Manual dependency injection (CONTEXT §5): each platform entry point builds one container
 * with its implementations and hands it to the shared UI.
 */
class AppContainer(
    val gitRepositoryFactory: GitRepositoryFactory,
    val settingsStore: SettingsStore,
)
