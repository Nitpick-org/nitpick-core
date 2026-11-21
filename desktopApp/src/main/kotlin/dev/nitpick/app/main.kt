package dev.nitpick.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.nitpick.app.git.JGitRepositoryFactory
import dev.nitpick.app.ui.App

fun main() {
    val container = AppContainer(gitRepositoryFactory = JGitRepositoryFactory())
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Nitpick",
        ) {
            App(container, pickRepositoryPath = { pickDirectory(window) })
        }
    }
}
