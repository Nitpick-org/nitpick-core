package dev.nitpick.app.settings

import kotlinx.serialization.Serializable

enum class LlmProvider { ANTHROPIC, OPENAI, GOOGLE, OLLAMA }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Serializable
data class Settings(
    val provider: LlmProvider = LlmProvider.ANTHROPIC,
    val model: String = LlmProvider.ANTHROPIC.knownModels.first(),
    val apiKey: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val recentRepositories: List<String> = emptyList(),
)

const val MAX_RECENT_REPOSITORIES = 10

/** Puts [path] first in the recent list, without duplicates, keeping the newest [MAX_RECENT_REPOSITORIES]. */
fun Settings.withRecentRepository(path: String): Settings =
    copy(recentRepositories = (listOf(path) + recentRepositories).distinct().take(MAX_RECENT_REPOSITORIES))

interface SettingsStore {
    suspend fun load(): Settings
    suspend fun save(settings: Settings)
}
