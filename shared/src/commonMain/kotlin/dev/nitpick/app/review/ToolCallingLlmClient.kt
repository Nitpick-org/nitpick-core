package dev.nitpick.app.review

/** A client that can run Koog's native tool-calling loop; plain clients stay on [LlmClient]. */
interface ToolCallingLlmClient : LlmClient {
    suspend fun completeWithTools(prompt: ReviewPrompt, toolbox: ReviewToolbox): String
}
