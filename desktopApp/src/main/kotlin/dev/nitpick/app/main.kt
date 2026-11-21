package dev.nitpick.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.nitpick.app.git.JGitRepositoryFactory
import dev.nitpick.app.settings.FileSettingsStore
import dev.nitpick.app.ui.App
import java.io.File

fun main() {
    val settingsFile = File(System.getProperty("user.home"), ".nitpick/settings.json")
    val container = AppContainer(
        gitRepositoryFactory = JGitRepositoryFactory(),
        settingsStore = FileSettingsStore(settingsFile),
    )
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Nitpick",
        ) {
            App(container, pickRepositoryPath = { pickDirectory(window) })
        }
    }
}
