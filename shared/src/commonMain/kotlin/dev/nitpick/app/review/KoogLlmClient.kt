package dev.nitpick.app.review

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.AnthropicLLMProvider
import ai.koog.prompt.llm.GoogleLLMProvider
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.llm.OllamaLLMProvider
import ai.koog.prompt.llm.OpenAILLMProvider
import ai.koog.prompt.message.MessagePart
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore

private const val OLLAMA_BASE_URL = "http://localhost:11434"
// A review is a JSON list: cut mid-way it is unusable, so ask for room every target model accepts.
private const val MAX_OUTPUT_TOKENS = 8_192L

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
        val model = koogModelFor(settings)
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

/**
 * The id comes from the settings select; the capabilities come from Koog's own declarations,
 * because its clients refuse a model declared without `Tools`, even for a plain completion.
 */
internal fun koogModelFor(settings: Settings): LLModel = when (settings.provider) {
    LlmProvider.ANTHROPIC -> AnthropicModels.Opus_5.copy(id = settings.model, maxOutputTokens = MAX_OUTPUT_TOKENS)
    LlmProvider.OPENAI -> LLModel(OpenAILLMProvider, settings.model, GENERIC_CAPABILITIES, maxOutputTokens = MAX_OUTPUT_TOKENS)
    LlmProvider.GOOGLE -> LLModel(GoogleLLMProvider, settings.model, GENERIC_CAPABILITIES, maxOutputTokens = MAX_OUTPUT_TOKENS)
    LlmProvider.OLLAMA -> LLModel(OllamaLLMProvider, settings.model, GENERIC_CAPABILITIES, maxOutputTokens = MAX_OUTPUT_TOKENS)
}

private val GENERIC_CAPABILITIES = listOf(LLMCapability.Completion, LLMCapability.Tools, LLMCapability.Temperature)

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
