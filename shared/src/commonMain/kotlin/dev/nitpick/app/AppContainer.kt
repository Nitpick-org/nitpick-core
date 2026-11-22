package dev.nitpick.app

import dev.nitpick.app.git.GitRepositoryFactory
import dev.nitpick.app.review.KoogLlmClient
import dev.nitpick.app.review.LlmReviewAgent
import dev.nitpick.app.review.ReviewAgent
import dev.nitpick.app.settings.ForgeClient
import dev.nitpick.app.settings.SettingsStore
import dev.nitpick.app.share.KtorReviewShareClient
import dev.nitpick.app.share.ReviewShareClient

/**
 * Manual dependency injection (CONTEXT §5): each platform entry point builds one container
 * with its implementations and hands it to the shared UI.
 */
class AppContainer(
    val gitRepositoryFactory: GitRepositoryFactory,
    val settingsStore: SettingsStore,
    val reviewAgent: ReviewAgent = LlmReviewAgent(KoogLlmClient(settingsStore)),
    val reviewShareClient: ReviewShareClient = KtorReviewShareClient({ settingsStore.load().serverUrl }, { settingsStore.load().serverToken }),
    val forgeClient: ForgeClient = ForgeClient(),
)
