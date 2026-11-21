package dev.nitpick.app.review

import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.ollama.client.OllamaModels
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore

private const val OLLAMA_BASE_URL = "http://localhost:11434"
// A review is a JSON list: cut mid-way it is unusable, so ask for room every target model accepts.
private const val MAX_OUTPUT_TOKENS = 8_192L
private const val MAX_TOOL_ROUNDS = 4

class MissingApiKeyException(val provider: LlmProvider) : Exception("No API key for ${provider.name}")

class ReviewProviderException(message: String, cause: Throwable) : Exception(message, cause)

/**
 * The only file that talks to Koog. Settings are read at call time so a key or model changed
 * in the UI applies to the next review without restarting the app.
 */
class KoogLlmClient(private val settingsStore: SettingsStore) : ToolCallingLlmClient {

    override suspend fun complete(system: String, user: String): String = withClient { client, model, _ ->
        val answer = client.execute(prompt("nitpick-review") { system(system); user(user) }, model)
        answer.parts.filterIsInstance<MessagePart.Text>().joinToString("") { it.text }
    }

    override suspend fun completeWithTools(system: String, user: String, toolbox: ReviewToolbox): String =
        withClient { client, model, settings ->
            var conversation = Prompt.builder("nitpick-review-tools").apply {
                system(system)
                user(user)
            }.build()
            val descriptors = toolbox.descriptors()

            // A provider without tool support must still review: fall back to the plain prompt.
            var answer = try {
                client.execute(conversation, model, descriptors)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                val plain = client.execute(conversation, model)
                return@withClient plain.parts.filterIsInstance<MessagePart.Text>().joinToString("") { it.text }
            }

            repeat(MAX_TOOL_ROUNDS) {
                val calls = answer.parts.filterIsInstance<MessagePart.Tool.Call>()
                if (calls.isEmpty()) {
                    return@withClient answer.parts.filterIsInstance<MessagePart.Text>().joinToString("") { it.text }
                }
                val next = Prompt.builder("nitpick-review-tools").apply {
                    messages(conversation.messages)
                    message(answer)
                    calls.forEach { call ->
                        val output = toolbox.execute(call.tool, call.argsJson)
                        toolResult(call.id.orEmpty(), call.tool, output.text, output.isError)
                    }
                }.build()
                conversation = next
                answer = client.execute(conversation, model, descriptors)
            }
            error("The ${settings.provider.name} review exceeded $MAX_TOOL_ROUNDS tool rounds")
        }

    private suspend fun <T> withClient(block: suspend (LLMClient, LLModel, Settings) -> T): T {
        val settings = settingsStore.load()
        if (settings.provider != LlmProvider.OLLAMA && settings.apiKey.isBlank()) {
            throw MissingApiKeyException(settings.provider)
        }
        val model = koogModelFor(settings)
        val client = llmClient(settings, model)
        try {
            return block(client, model, settings)
        } catch (e: LLMClientException) {
            // Provider errors can echo request headers: never forward their text as-is.
            throw ReviewProviderException("The ${settings.provider.name} request failed (${e::class.simpleName})", e)
        } finally {
            client.close()
        }
    }
}

/**
 * The id comes from the settings select; the capabilities come from a model Koog ships for
 * the same provider, because its clients refuse a model declared without `Tools` (Anthropic)
 * or without an endpoint capability (OpenAI), even for a plain completion. `Thinking` is
 * dropped: a review needs the answer text, not reasoning blocks to pay for.
 */
internal fun koogModelFor(settings: Settings): LLModel {
    val reference = when (settings.provider) {
        LlmProvider.ANTHROPIC -> AnthropicModels.Opus_5
        LlmProvider.OPENAI -> OpenAIModels.Chat.GPT5
        LlmProvider.GOOGLE -> GoogleModels.Gemini2_5Pro
        LlmProvider.OLLAMA -> OllamaModels.Meta.LLAMA_4
    }
    return reference.copy(
        id = settings.model,
        capabilities = reference.capabilities.orEmpty().filterNot { it is LLMCapability.Thinking },
        maxOutputTokens = MAX_OUTPUT_TOKENS,
    )
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
