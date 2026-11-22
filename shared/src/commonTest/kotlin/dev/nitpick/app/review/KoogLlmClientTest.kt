package dev.nitpick.app.review

import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.llm.LLMCapability

class KoogLlmClientTest {

    private fun store(settings: Settings) = object : SettingsStore {
        override suspend fun load() = settings
        override suspend fun save(settings: Settings) = Unit
    }

    @Test
    fun refusesToCallAProviderWithoutAKey() = runTest {
        // Ollama is local and keyless; every hosted provider must stop before any request.
        listOf(LlmProvider.ANTHROPIC, LlmProvider.OPENAI, LlmProvider.GOOGLE).forEach { provider ->
            val client = KoogLlmClient(store(Settings(provider = provider, apiKey = " ")))
            val error = assertFailsWith<MissingApiKeyException> { client.complete("s", "u") }
            assertEquals(provider, error.provider)
        }
    }

    @Test
    fun onlyAToolUnsupportedErrorAllowsTheNoToolFallback() {
        assertTrue(LLMClientException("model does not support tools", "anthropic", null).allowsToolFallback())
        assertTrue(LLMClientException("tools are unsupported", "openai", null).allowsToolFallback())
        assertTrue(UnsupportedOperationException("no tools").allowsToolFallback())
        assertFalse(LLMClientException("401 Unauthorized", "anthropic", null).allowsToolFallback())
        assertFalse(LLMClientException("429 rate limit", "anthropic", null).allowsToolFallback())
        assertFalse(IllegalStateException("tool call failed").allowsToolFallback())
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
