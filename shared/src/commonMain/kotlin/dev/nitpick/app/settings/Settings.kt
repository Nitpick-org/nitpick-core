package dev.nitpick.app.settings

import dev.nitpick.app.domain.Credentials
import kotlinx.serialization.Serializable

enum class LlmProvider { ANTHROPIC, OPENAI, GOOGLE, OLLAMA }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class GitHost { GITHUB, GITLAB }

/** The forge account the phone clones from and pushes to: one token, typed once. */
@Serializable
data class GitConnection(
    val host: GitHost = GitHost.GITHUB,
    val token: String = "",
) {
    val isComplete: Boolean get() = token.isNotBlank()

    // GitHub accepts any user name with a token; GitLab expects "oauth2" for tokens over HTTPS.
    fun toCredentials(): Credentials =
        Credentials(username = if (host == GitHost.GITLAB) "oauth2" else "x-access-token", token = token)
}

@Serializable
data class Settings(
    val provider: LlmProvider = LlmProvider.ANTHROPIC,
    val model: String = LlmProvider.ANTHROPIC.knownModels.first(),
    val apiKey: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val recentRepositories: List<String> = emptyList(),
    val gitConnection: GitConnection? = null,
    val serverUrl: String = DEFAULT_SERVER_URL,
)

const val MAX_RECENT_REPOSITORIES = 10

// The review-sharing server of the laptop; a phone reaches it through adb reverse or the LAN IP.
const val DEFAULT_SERVER_URL = "http://localhost:8080"

/** Puts [path] first in the recent list, without duplicates, keeping the newest [MAX_RECENT_REPOSITORIES]. */
fun Settings.withRecentRepository(path: String): Settings =
    copy(recentRepositories = (listOf(path) + recentRepositories).distinct().take(MAX_RECENT_REPOSITORIES))

interface SettingsStore {
    suspend fun load(): Settings
    suspend fun save(settings: Settings)
}
