package dev.nitpick.app

import dev.nitpick.app.git.GitRepositoryFactory

/**
 * Manual dependency injection (docs/CONTEXT.md §5): each platform entry point builds one
 * container with its implementations and hands it to the shared UI.
 */
class AppContainer(
    val gitRepositoryFactory: GitRepositoryFactory,
)
