package dev.nitpick.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.nitpick.app.git.JGitRepositoryFactory
import dev.nitpick.app.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // No folder picker on Android: repositories arrive through the clone ticket (#21).
        val container = AppContainer(gitRepositoryFactory = JGitRepositoryFactory())
        setContent {
            App(container)
        }
    }
}
