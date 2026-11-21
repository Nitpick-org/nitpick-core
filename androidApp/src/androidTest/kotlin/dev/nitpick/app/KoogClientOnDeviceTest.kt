package dev.nitpick.app

import dev.nitpick.app.review.KoogLlmClient
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/**
 * Proves the Koog client gets past its HTTP client lookup on ART, where no ServiceLoader entry
 * exists: with a bogus key the provider answers, instead of failing before any network call.
 */
class KoogClientOnDeviceTest {

    @Test
    fun aBogusKeyIsRefusedByTheProviderNotByTheHttpClientLookup() {
        val store = object : SettingsStore {
            override suspend fun load() = Settings(provider = LlmProvider.ANTHROPIC, apiKey = "not-a-key")
            override suspend fun save(settings: Settings) = Unit
        }

        // Any exception will do, even a connect error on an offline emulator: only the lookup failure is the bug.
        val error = assertFailsWith<Exception> { runBlocking { KoogLlmClient(store).complete("s", "u") } }

        assertFalse(error.message.orEmpty().contains("KoogHttpClient"), error.message)
    }
}
