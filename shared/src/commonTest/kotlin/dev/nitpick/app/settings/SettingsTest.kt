package dev.nitpick.app.settings

import dev.nitpick.app.domain.Credentials
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsTest {

    @Test
    fun defaultsFollowSystemThemeWithAnthropicAndNoKey() {
        val settings = Settings()
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertEquals(LlmProvider.ANTHROPIC, settings.provider)
        assertEquals(LlmProvider.ANTHROPIC.knownModels.first(), settings.model)
        assertEquals("", settings.apiKey)
        assertEquals(emptyList(), settings.recentRepositories)
        assertEquals(null, settings.gitConnection)
        assertEquals(AppLanguage.SYSTEM, settings.language)
        assertEquals(null, AppLanguage.SYSTEM.tag)
        assertEquals("fr", AppLanguage.FRENCH.tag)
    }

    @Test
    fun theConnectionTurnsIntoTheUserNameEachForgeExpects() {
        assertEquals(Credentials("x-access-token", "ghp_1"), GitConnection(GitHost.GITHUB, "ghp_1").toCredentials())
        assertEquals(Credentials("oauth2", "glpat_1"), GitConnection(GitHost.GITLAB, "glpat_1").toCredentials())
        assertEquals(false, GitConnection(GitHost.GITHUB, " ").isComplete)
    }

    @Test
    fun theConnectionSurvivesTheSettingsFile() {
        val settings = Settings(gitConnection = GitConnection(GitHost.GITLAB, "glpat_1"), language = AppLanguage.FRENCH)
        val json = Json.encodeToString(Settings.serializer(), settings)
        assertEquals(settings, Json.decodeFromString(Settings.serializer(), json))
    }

    @Test
    fun aSettingsFileWrittenBeforeTheLoginFieldStillLoads() {
        val old = """{"gitConnection":{"host":"GITHUB","token":"ghp_1"}}"""
        assertEquals(GitConnection(GitHost.GITHUB, "ghp_1"), Json.decodeFromString(Settings.serializer(), old).gitConnection)
    }

    @Test
    fun everyProviderSuggestsAtLeastOneModel() {
        LlmProvider.entries.forEach { provider ->
            assertTrue(provider.knownModels.isNotEmpty(), provider.name)
        }
    }

    @Test
    fun aRecentRepositoryMovesToTheFrontWithoutDuplicates() {
        val settings = Settings(recentRepositories = listOf("/a", "/b"))

        assertEquals(listOf("/b", "/a"), settings.withRecentRepository("/b").recentRepositories)
        assertEquals(listOf("/c", "/a", "/b"), settings.withRecentRepository("/c").recentRepositories)
    }

    @Test
    fun recentRepositoriesAreCappedToTheNewestOnes() {
        val full = Settings(recentRepositories = (1..MAX_RECENT_REPOSITORIES).map { "/repo$it" })

        val recent = full.withRecentRepository("/new").recentRepositories

        assertEquals(MAX_RECENT_REPOSITORIES, recent.size)
        assertEquals("/new", recent.first())
        assertEquals("/repo${MAX_RECENT_REPOSITORIES - 1}", recent.last())
    }
}
