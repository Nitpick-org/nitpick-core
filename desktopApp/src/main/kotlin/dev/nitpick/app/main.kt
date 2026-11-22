package dev.nitpick.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.nitpick.app.git.JGitRepositoryFactory
import dev.nitpick.app.review.JunieCliLlmClient
import dev.nitpick.app.review.KoogLlmClient
import dev.nitpick.app.review.LlmReviewAgent
import dev.nitpick.app.settings.FileSettingsStore
import dev.nitpick.app.ui.App
import dev.nitpick.app.ui.nitpickAppIcon
import java.io.File

fun main() {
    val settingsFile = File(System.getProperty("user.home"), ".nitpick/settings.json")
    val settingsStore = FileSettingsStore(settingsFile)
    val container = AppContainer(
        gitRepositoryFactory = JGitRepositoryFactory(),
        settingsStore = settingsStore,
        reviewAgent = LlmReviewAgent(KoogLlmClient(settingsStore, junie = JunieCliLlmClient(settingsStore))),
    )
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Nitpick",
            icon = nitpickAppIcon(),
        ) {
            App(container, pickRepositoryPath = { pickDirectory(window) })
        }
    }
}
