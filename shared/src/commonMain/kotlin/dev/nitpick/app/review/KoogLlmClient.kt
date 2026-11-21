package dev.nitpick.app.review

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.AnthropicLLMProvider
import ai.koog.prompt.llm.GoogleLLMProvider
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.llm.OllamaLLMProvider
import ai.koog.prompt.llm.OpenAILLMProvider
import ai.koog.prompt.message.MessagePart
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore

private const val OLLAMA_BASE_URL = "http://localhost:11434"

class MissingApiKeyException(val provider: LlmProvider) : Exception("No API key for ${provider.name}")

class ReviewProviderException(message: String, cause: Throwable) : Exception(message, cause)

/**
 * The only file that talks to Koog. Settings are read at call time so a key or model changed
 * in the UI applies to the next review without restarting the app.
 */
class KoogLlmClient(private val settingsStore: SettingsStore) : LlmClient {

    override suspend fun complete(system: String, user: String): String {
        val settings = settingsStore.load()
        if (settings.provider != LlmProvider.OLLAMA && settings.apiKey.isBlank()) {
            throw MissingApiKeyException(settings.provider)
        }
        val model = llModel(settings)
        val client = llmClient(settings, model)
        try {
            val answer = client.execute(prompt("nitpick-review") { system(system); user(user) }, model)
            return answer.parts.filterIsInstance<MessagePart.Text>().joinToString("") { it.text }
        } catch (e: LLMClientException) {
            // Provider errors can echo request headers: never forward their text as-is.
            throw ReviewProviderException("The ${settings.provider.name} request failed (${e::class.simpleName})", e)
        } finally {
            client.close()
        }
    }
}

// Built from the id the settings select stores, so the list in the UI is the truth.
private fun llModel(settings: Settings): LLModel = LLModel(
    provider = settings.provider.koogProvider,
    id = settings.model,
    capabilities = listOf(LLMCapability.Completion),
)

private val LlmProvider.koogProvider: LLMProvider
    get() = when (this) {
        LlmProvider.ANTHROPIC -> AnthropicLLMProvider
        LlmProvider.OPENAI -> OpenAILLMProvider
        LlmProvider.GOOGLE -> GoogleLLMProvider
        LlmProvider.OLLAMA -> OllamaLLMProvider
    }

private fun llmClient(settings: Settings, model: LLModel): LLMClient = when (settings.provider) {
    // Koog 1.3.0 only sends models listed in its version map, so a newer id needs its own entry.
    LlmProvider.ANTHROPIC -> AnthropicLLMClient(
        apiKey = settings.apiKey,
        settings = AnthropicClientSettings(modelVersionsMap = mapOf(model to model.id)),
    )
    LlmProvider.OPENAI -> OpenAILLMClient(apiKey = settings.apiKey)
    LlmProvider.GOOGLE -> GoogleLLMClient(apiKey = settings.apiKey)
    LlmProvider.OLLAMA -> OllamaClient(baseUrl = OLLAMA_BASE_URL)
}
