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
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Acceptance proof of #20: one real call against Anthropic. Opt-in, so check.sh never spends
 * money: run with APP_ANTHROPIC_API_KEY set (the app key, never ANTHROPIC_API_KEY).
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
    fun aRealCallReturnsAtLeastOneAnchoredNit() = runBlocking {
        val apiKey = System.getenv("APP_ANTHROPIC_API_KEY")
        if (apiKey.isNullOrBlank()) {
            println("RealReviewCallTest skipped: APP_ANTHROPIC_API_KEY is not set")
            return@runBlocking
        }
        val model = System.getenv("APP_ANTHROPIC_MODEL") ?: Settings().model
        val store = object : SettingsStore {
            override suspend fun load() = Settings(provider = LlmProvider.ANTHROPIC, model = model, apiKey = apiKey)
            override suspend fun save(settings: Settings) = Unit
        }
        val lines = buggy.trimEnd().lines().mapIndexed { i, l -> DiffLine(DiffLineType.ADDED, l, null, i + 1) }
        val diff = FileDiff("Last.kt", null, FileChangeType.ADDED, false, listOf(Hunk(0, 0, 1, lines.size, lines)))
        val repository = FakeGitRepository(branch = "feature", diffs = mapOf("main" to listOf(diff)), files = mapOf("Last.kt" to buggy))

        val result = LlmReviewAgent(KoogLlmClient(store)).review(repository, "main")

        println("summary: ${result.summary}")
        result.comments.forEach { println("${it.severity} ${it.filePath}:${it.startLine}-${it.endLine} ${it.message}") }
        assertTrue(result.comments.any { !it.isFileLevel }, "expected at least one nit anchored on a line")
    }
}
