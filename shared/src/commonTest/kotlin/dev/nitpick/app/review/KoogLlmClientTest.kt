package dev.nitpick.app.review

import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import ai.koog.prompt.llm.LLMCapability

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

    @Test
    fun aModelUnknownToKoogKeepsItsIdAndGetsKoogsCapabilities() {
        val model = koogModelFor(Settings(provider = LlmProvider.ANTHROPIC, model = "claude-opus-5-5"))
        assertEquals("claude-opus-5-5", model.id)
        assertTrue(LLMCapability.Tools in model.capabilities.orEmpty())
        assertTrue(LLMCapability.Completion in model.capabilities.orEmpty())
    }
}
