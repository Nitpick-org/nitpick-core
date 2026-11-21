package dev.nitpick.app.review

import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KoogLlmClientTest {

    private fun store(settings: Settings) = object : SettingsStore {
        override suspend fun load() = settings
        override suspend fun save(settings: Settings) = Unit
    }

    @Test
    fun refusesToCallAProviderWithoutAKey() = runTest {
        val client = KoogLlmClient(store(Settings(provider = LlmProvider.ANTHROPIC, apiKey = " ")))
        val error = assertFailsWith<MissingApiKeyException> { client.complete("s", "u") }
        assertEquals(LlmProvider.ANTHROPIC, error.provider)
    }
}
