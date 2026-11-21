package dev.nitpick.app.settings

// shortcut: static list, upgrade to a live provider lookup if a model is retired before the
// demo. OpenAI, Google and Ollama ids are not verified against their current catalogs.
val LlmProvider.knownModels: List<String>
    get() = when (this) {
        LlmProvider.ANTHROPIC -> listOf("claude-opus-5-5", "claude-sonnet-5-5", "claude-haiku-5-5")
        LlmProvider.OPENAI -> listOf("gpt-5", "gpt-5-mini", "gpt-4.1")
        LlmProvider.GOOGLE -> listOf("gemini-2.5-pro", "gemini-2.5-flash")
        LlmProvider.OLLAMA -> listOf("llama3.1", "qwen2.5-coder", "mistral")
    }
