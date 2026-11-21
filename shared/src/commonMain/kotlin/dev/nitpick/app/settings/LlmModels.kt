package dev.nitpick.app.settings

// shortcut: static list, upgrade to a live provider lookup if a model is retired before the
// demo. Anthropic ids come from the Claude API docs; the others are the newest entries of
// Koog 1.3.0's own catalogues (OpenAIModels, GoogleModels, OllamaModels).
val LlmProvider.knownModels: List<String>
    get() = when (this) {
        LlmProvider.ANTHROPIC -> listOf("claude-opus-5-5", "claude-sonnet-5-5", "claude-haiku-5-5")
        LlmProvider.OPENAI -> listOf("gpt-5.5", "gpt-5.4", "gpt-5.4-mini")
        LlmProvider.GOOGLE -> listOf("gemini-3.7-flash", "gemini-3.1-pro-preview", "gemini-2.5-pro")
        LlmProvider.OLLAMA -> listOf("llama3.2:latest", "qwen2.5-coder:32b", "deepseek-r1:1.5b")
    }
