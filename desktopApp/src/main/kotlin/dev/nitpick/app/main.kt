package dev.nitpick.app

import androidx.compose.ui.window.Window
import dev.nitpick.app.ui.App
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Nitpick",
    ) {
        App()
    }
}