package dev.nitpick.app.review

import dev.nitpick.app.domain.DiffLine
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.FileChangeType
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.Hunk
import dev.nitpick.app.git.FakeGitRepository
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore
import dev.nitpick.app.settings.knownModels
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Acceptance proof of #20 and #82: one real review per provider. Opt-in, so check.sh never
 * spends money: each test skips unless APP_<PROVIDER>_API_KEY is set (the app keys, never
 * ANTHROPIC_API_KEY), or, for Ollama, unless localhost:11434 answers.
 * APP_<PROVIDER>_MODEL overrides the first known model. Keys are never printed.
 */
class RealReviewCallTest {

    private val buggy = """
        fun lastOf(xs: List<Int>): Int? {
            for (i in 0 until xs.size - 1) {
                if (i == xs.size - 1) return xs[i]
            }
            return null
        }
    """.trimIndent() + "\n"

    @Test
    fun anthropic() = reviewWith(LlmProvider.ANTHROPIC)

    @Test
    fun openAi() = reviewWith(LlmProvider.OPENAI)

    @Test
    fun google() = reviewWith(LlmProvider.GOOGLE)

    @Test
    fun ollama() = reviewWith(LlmProvider.OLLAMA)

    private fun reviewWith(provider: LlmProvider) = runBlocking {
        val apiKey = System.getenv("APP_${provider.name}_API_KEY").orEmpty()
        val available = if (provider == LlmProvider.OLLAMA) ollamaIsUp() else apiKey.isNotBlank()
        if (!available) {
            val missing = if (provider == LlmProvider.OLLAMA) "localhost:11434 is not reachable" else "APP_${provider.name}_API_KEY is not set"
            println("RealReviewCallTest.${provider.name} skipped: $missing")
            return@runBlocking
        }
        val model = System.getenv("APP_${provider.name}_MODEL") ?: provider.knownModels.first()
        val store = object : SettingsStore {
            override suspend fun load() = Settings(provider = provider, model = model, apiKey = apiKey)
            override suspend fun save(settings: Settings) = Unit
        }
        val lines = buggy.trimEnd().lines().mapIndexed { i, l -> DiffLine(DiffLineType.ADDED, l, null, i + 1) }
        val diff = FileDiff("Last.kt", null, FileChangeType.ADDED, false, listOf(Hunk(0, 0, 1, lines.size, lines)))
        val repository = FakeGitRepository(branch = "feature", diffs = mapOf("main" to listOf(diff)), files = mapOf("Last.kt" to buggy))

        val result = LlmReviewAgent(KoogLlmClient(store)).review(repository, "main")

        println("${provider.name} $model summary: ${result.summary}")
        result.comments.forEach { println("${it.severity} ${it.filePath}:${it.startLine}-${it.endLine} ${it.message}") }
        println("tools used: ${result.toolUses.size}")
        assertTrue(result.comments.any { !it.isFileLevel }, "expected at least one nit anchored on a line from ${provider.name}")
    }

    private fun ollamaIsUp(): Boolean = try {
        Socket().use { it.connect(InetSocketAddress("localhost", 11434), 500) }
        true
    } catch (e: java.io.IOException) {
        false
    }
}
