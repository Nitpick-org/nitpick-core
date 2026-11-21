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
    fun aModelUnknownToKoogKeepsItsIdAndGetsKoogsCapabilitiesForEveryProvider() {
        LlmProvider.entries.forEach { provider ->
            val model = koogModelFor(Settings(provider = provider, model = "next-gen-model"))
            assertEquals("next-gen-model", model.id, provider.name)
            assertTrue(model.capabilities.orEmpty().isNotEmpty(), provider.name)
            assertTrue(model.capabilities.orEmpty().none { it is LLMCapability.Thinking }, provider.name)
        }
        // These two clients are known to refuse a model declared without Tools.
        listOf(LlmProvider.ANTHROPIC, LlmProvider.OPENAI).forEach { provider ->
            val model = koogModelFor(Settings(provider = provider, model = "next-gen-model"))
            assertTrue(LLMCapability.Tools in model.capabilities.orEmpty(), provider.name)
        }
    }
}
