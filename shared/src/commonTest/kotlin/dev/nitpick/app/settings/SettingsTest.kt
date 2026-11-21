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
        assertEquals("", settings.model)
        assertEquals("", settings.apiKey)
        assertEquals(emptyList(), settings.recentRepositories)
    }

    @Test
    fun everyProviderSuggestsAtLeastOneModel() {
        LlmProvider.entries.forEach { provider ->
            assertTrue(provider.knownModels.isNotEmpty(), provider.name)
        }
    }
}
