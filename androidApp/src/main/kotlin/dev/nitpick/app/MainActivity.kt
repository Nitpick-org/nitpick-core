package dev.nitpick.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.nitpick.app.git.GitIdentity
import dev.nitpick.app.git.JGitRepositoryFactory
import dev.nitpick.app.settings.FileSettingsStore
import dev.nitpick.app.ui.App
import java.io.File

private var sharedContainer: AppContainer? = null

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // One container per process: a rotation recreates the activity, not the HTTP clients.
        val container = sharedContainer ?: AppContainer(
            // shortcut: a fixed identity until the forge login is fetched with the token (#64).
            gitRepositoryFactory = JGitRepositoryFactory(GitIdentity("Nitpick on Android", "nitpick-android@users.noreply.github.com")),
            // No folder picker on Android: repositories are cloned into the app's private storage.
            settingsStore = FileSettingsStore(File(filesDir, "settings.json")),
        ).also { sharedContainer = it }
        val cloneRoot = File(filesDir, "repos")
        setContent {
            App(container, cloneRoot = cloneRoot.path)
        }
    }
}
