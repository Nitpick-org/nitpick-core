package dev.nitpick.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.nitpick.app.git.JGitRepositoryFactory
import dev.nitpick.app.settings.FileSettingsStore
import dev.nitpick.app.ui.App
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // No folder picker on Android: repositories are cloned into the app's private storage.
        val container = AppContainer(
            gitRepositoryFactory = JGitRepositoryFactory(),
            settingsStore = FileSettingsStore(File(filesDir, "settings.json")),
        )
        val cloneRoot = File(filesDir, "repos")
        setContent {
            App(container, cloneRoot = cloneRoot.path)
        }
    }
}
