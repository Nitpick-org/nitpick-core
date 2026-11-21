package dev.nitpick.app.settings

enum class LlmProvider { ANTHROPIC, OPENAI, GOOGLE, OLLAMA }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Settings(
    val provider: LlmProvider = LlmProvider.ANTHROPIC,
    val model: String = "",
    val apiKey: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val recentRepositories: List<String> = emptyList(),
)

interface SettingsStore {
    suspend fun load(): Settings
    suspend fun save(settings: Settings)
}
