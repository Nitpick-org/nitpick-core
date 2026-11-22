package dev.nitpick.app.review

/** One completion call: the only boundary the review pipeline needs from an LLM provider. */
interface LlmClient {
    suspend fun complete(system: String, user: String): String
}

/** The Junie provider has no CLI to run here: not installed, not signed in, or not a desktop. */
class JunieUnavailableException(message: String, cause: Throwable? = null) : Exception(message, cause)
