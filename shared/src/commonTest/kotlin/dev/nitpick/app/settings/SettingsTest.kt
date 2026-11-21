package dev.nitpick.app.settings

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
