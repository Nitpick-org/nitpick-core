package dev.nitpick.app.review

/** One completion call: the only boundary the review pipeline needs from an LLM provider. */
interface LlmClient {
    suspend fun complete(system: String, user: String): String
}
