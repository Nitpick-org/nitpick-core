package dev.nitpick.app.review

import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.ollama.client.OllamaModels
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicParams
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore

private const val OLLAMA_BASE_URL = "http://localhost:11434"
// A review is a JSON list: cut mid-way it is unusable. Newer Anthropic models spend part of
// the budget on adaptive reasoning, so 8k was entirely consumed before any JSON was emitted.
private const val MAX_OUTPUT_TOKENS = 32_768L
private const val MAX_TOOL_ROUNDS = 4

class MissingApiKeyException(val provider: LlmProvider) : Exception("No API key for ${provider.name}")

class ReviewProviderException(message: String, cause: Throwable) : Exception(message, cause)

/**
 * The only file that talks to Koog. Settings are read at call time so a key or model changed
 * in the UI applies to the next review without restarting the app.
 */
class KoogLlmClient(
    private val settingsStore: SettingsStore,
    // Test hook: points the OpenAI client at an OpenAI-compatible server (LM Studio) instead of api.openai.com.
    private val openAiBaseUrl: String? = null,
    // Desktop passes the Junie CLI; without it the JUNIE provider fails with a clear message.
    private val junie: LlmClient? = null,
) : ToolCallingLlmClient {

    override suspend fun complete(system: String, user: String): String {
        junieIfSelected()?.let { return it.complete(system, user) }
        return withClient { client, model, settings ->
            val answer = client.execute(prompt("nitpick-review") { system(system); user(user) }.withReviewParams(settings), model)
            textOf(answer)
        }
    }

    override suspend fun completeWithTools(prompt: ReviewPrompt, toolbox: ReviewToolbox): String {
        // Junie runs its own tools in a sandbox: never advertise ours to it.
        junieIfSelected()?.let { val plain = prompt.withoutTools(); return it.complete(plain.system, plain.user) }
        return withClient { client, model, settings ->
            var conversation = Prompt.builder("nitpick-review-tools").apply {
                system(prompt.system)
                user(prompt.user)
            }.build().withReviewParams(settings)
            val descriptors = toolbox.descriptors()

            // A provider without tool support must still review: fall back to the plain prompt.
            var answer = try {
                client.execute(conversation, model, descriptors)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                // Only a provider/tool-support failure may fall back; a 401/429 or a programming
                // error must surface without a second call (#146).
                if (!e.allowsToolFallback()) throw e
                val plainPrompt = prompt.withoutTools()
                val plain = Prompt.builder("nitpick-review").apply {
                    system(plainPrompt.system)
                    user(plainPrompt.user)
                }.build().withReviewParams(settings)
                val plainAnswer = try {
                    client.execute(plain, model)
                } catch (plainError: Exception) {
                    if (plainError is kotlinx.coroutines.CancellationException) throw plainError
                    throw e
                }
                return@withClient textOf(plainAnswer)
            }

            // One counter for the whole conversation: a second round must not reuse tool_0.
            var toolCounter = 0
            repeat(MAX_TOOL_ROUNDS) {
                val calls = answer.parts.filterIsInstance<MessagePart.Tool.Call>()
                if (calls.isEmpty()) {
                    return@withClient textOf(answer)
                }
                // Anthropic can return a reasoning block even when the model is declared without
                // Thinking; drop it before echoing the assistant turn, or the next request fails.
                // Tool ids are also normalized: provider ids are opaque and some contain characters
                // Anthropic rejects in tool_result.tool_use_id.
                val ids = List(calls.size) { "tool_${toolCounter++}" }
                var safeIndex = 0
                val safeParts = answer.parts
                    .filterNot { it is MessagePart.Reasoning }
                    .map { part ->
                        if (part is MessagePart.Tool.Call) part.copy(id = ids[safeIndex++]) else part
                    }
                val assistant = answer.copy(parts = safeParts)
                val next = Prompt.builder("nitpick-review-tools").apply {
                    messages(conversation.messages)
                    message(assistant)
                    calls.forEachIndexed { index, call ->
                        val output = toolbox.execute(call.tool, call.argsJson)
                        toolResult(
                            tool = call.tool,
                            output = output.text,
                            id = ids[index],
                            isError = output.isError,
                        )
                    }
                    // Tool results are not a review: force the final answer back to the JSON contract.
                    user(
                        "The tool result is above. Now return the final review as ONE JSON object " +
                            "exactly as specified; no prose, no more tool calls.",
                    )
                }.build()
                conversation = next
                answer = client.execute(conversation, model, descriptors)
            }
            // The last round may already contain the final answer: do not throw it away (#148).
            if (answer.parts.none { it is MessagePart.Tool.Call }) {
                return@withClient textOf(answer)
            }
            throw ReviewProviderException(
                "The ${settings.provider.name} review exceeded $MAX_TOOL_ROUNDS tool rounds",
                IllegalStateException("tool-call loop did not converge"),
            )
        }
    }

    private suspend fun junieIfSelected(): LlmClient? {
        if (settingsStore.load().provider != LlmProvider.JUNIE) return null
        return junie ?: throw JunieUnavailableException("The Junie CLI runs on desktop only")
    }

    private suspend fun <T> withClient(block: suspend (LLMClient, LLModel, Settings) -> T): T {
        val settings = settingsStore.load()
        if (settings.provider != LlmProvider.OLLAMA && settings.apiKey.isBlank()) {
            throw MissingApiKeyException(settings.provider)
        }
        val model = koogModelFor(settings)
        // The Android Koog AAR ships no KoogHttpClient.Factory service entry: pass one explicitly.
        val client = llmClient(settings, model, KtorKoogHttpClient.Factory(), openAiBaseUrl)
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
        LlmProvider.JUNIE -> error("Junie does not go through Koog")
    }
    return reference.copy(
        id = settings.model,
        capabilities = reference.capabilities.orEmpty().filterNot { it is LLMCapability.Thinking },
        maxOutputTokens = MAX_OUTPUT_TOKENS,
    )
}

/** Recent Anthropic models default to adaptive thinking; ask for low effort so JSON has room. */
private fun Prompt.withReviewParams(settings: Settings): Prompt =
    if (settings.provider == LlmProvider.ANTHROPIC) {
        withParams(
            AnthropicParams(
                additionalProperties = mapOf(
                    "output_config" to buildJsonObject { put("effort", "low") },
                ),
            ),
        )
    } else {
        this
    }

private fun textOf(answer: Message.Assistant): String {
    val text = answer.parts.filterIsInstance<MessagePart.Text>().joinToString("") { it.text }
    return text
}

private fun llmClient(settings: Settings, model: LLModel, httpClientFactory: KtorKoogHttpClient.Factory, openAiBaseUrl: String?): LLMClient = when (settings.provider) {
    // Koog 1.3.0 only sends models listed in its version map, so a newer id needs its own entry.
    LlmProvider.ANTHROPIC -> AnthropicLLMClient(
        apiKey = settings.apiKey,
        settings = AnthropicClientSettings(modelVersionsMap = mapOf(model to model.id)),
        httpClientFactory = httpClientFactory,
    )
    LlmProvider.OPENAI -> OpenAILLMClient(
        apiKey = settings.apiKey,
        settings = openAiBaseUrl?.let { OpenAIClientSettings(baseUrl = it) } ?: OpenAIClientSettings(),
        httpClientFactory = httpClientFactory,
    )
    LlmProvider.GOOGLE -> GoogleLLMClient(apiKey = settings.apiKey, httpClientFactory = httpClientFactory)
    LlmProvider.OLLAMA -> OllamaClient(baseUrl = OLLAMA_BASE_URL, httpClientFactory = httpClientFactory)
    LlmProvider.JUNIE -> error("Junie does not go through Koog")
}


/**
 * Only a provider that explicitly refuses tools may trigger the no-tool fallback. Koog's
 * [LLMClientException] has no status code, so this is a narrow message check; a 401/429 must
 * not double the call (#146).
 */
internal fun Throwable.allowsToolFallback(): Boolean =
    this is UnsupportedOperationException || (this is LLMClientException && isToolUnsupportedMessage())

private fun Throwable.isToolUnsupportedMessage(): Boolean {
    val text = generateSequence(this as Throwable?) { it.cause }
        .mapNotNull { it.message }
        .joinToString(" ")
        .lowercase()
    return "tool" in text && ("not support" in text || "unsupported" in text || "does not support" in text)
}
