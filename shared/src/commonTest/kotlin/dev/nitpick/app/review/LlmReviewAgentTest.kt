package dev.nitpick.app.review

import dev.nitpick.app.domain.DiffLine
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.FileChangeType
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.Hunk
import dev.nitpick.app.domain.Severity
import dev.nitpick.app.git.FakeGitRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LlmReviewAgentTest {

    private val content = "fun main() {\n    val xs = listOf(1)\n    for (i in 0 until xs.size - 1) println(xs[i])\n}\n"
    private val diff = FileDiff(
        path = "Main.kt", oldPath = null, changeType = FileChangeType.MODIFIED, isBinary = false,
        hunks = listOf(Hunk(1, 4, 1, 4, content.trimEnd().lines().mapIndexed { i, l -> DiffLine(DiffLineType.ADDED, l, null, i + 1) })),
    )
    private val repository = FakeGitRepository(
        branch = "feature",
        diffs = mapOf("main" to listOf(diff)),
        files = mapOf("Main.kt" to content, "README.md" to "# Demo"),
    )

    private class RepairingClient : LlmClient {
        var calls = 0
        override suspend fun complete(system: String, user: String): String {
            calls++
            return if (calls == 1) "I read Pricing.kt and found nothing." else """{"summary":"clean","comments":[]}"""
        }
    }

    private class RecordingClient(private val answer: String) : LlmClient {
        var user = ""
        override suspend fun complete(system: String, user: String): String { this.user = user; return answer }
    }

    private class ToolRecordingClient : ToolCallingLlmClient {
        var called = false
        var sawTools = false
        override suspend fun complete(system: String, user: String): String = "not used"
        override suspend fun completeWithTools(prompt: ReviewPrompt, toolbox: ReviewToolbox): String {
            called = true
            sawTools = "Tools:" in prompt.system
            return """{"summary":"","comments":[]}"""
        }
    }

    @Test
    fun reviewsTheDiffAndReAnchorsAShiftedBug() = runTest {
        val client = RecordingClient(
            """{"summary":"1 bug","comments":[
               {"filePath":"Main.kt","startLine":1,"endLine":1,"severity":"BUG","category":"logic",
                "message":"last item skipped","originalSnippet":"for (i in 0 until xs.size - 1) println(xs[i])",
                "suggestion":"    for (i in xs.indices) println(xs[i])"},
               {"filePath":"Other.kt","startLine":1,"endLine":1,"severity":"INFO","category":"other","message":"not in diff","originalSnippet":""}
            ]}""",
        )

        val result = LlmReviewAgent(client).review(repository, "main")

        assertContains(client.user, "    3 | +    for (i in 0 until xs.size - 1) println(xs[i])")
        val nit = result.comments.single()
        assertEquals(Severity.BUG, nit.severity)
        assertEquals(3 to 3, nit.startLine to nit.endLine)
    }

    @Test
    fun aProseAnswerIsRetriedAsJson() = runTest {
        val client = RepairingClient()

        val result = LlmReviewAgent(client).review(repository, "main")

        assertEquals(2, client.calls)
        assertEquals("clean", result.summary)
    }

    @Test
    fun aToolCapableClientGetsTheToolboxAndTheToolPrompt() = runTest {
        val client = ToolRecordingClient()

        LlmReviewAgent(client).review(repository, "main")

        assertTrue(client.called)
        assertTrue(client.sawTools)
    }

    @Test
    fun anEmptyDiffNeverCallsTheModel() = runTest {
        val client = RecordingClient("not json")
        val result = LlmReviewAgent(client).review(FakeGitRepository(diffs = mapOf("main" to emptyList())), "main")
        assertTrue(result.comments.isEmpty())
        assertEquals("", client.user)
    }

    @Test
    fun aNitOnAFileTooBigForThePromptKeepsItsLine() = runTest {
        val bigContent = buildString { repeat(2_000) { appendLine("val filler$it = $it") }; append(content) }
        val lineOfBug = 2_000 + 3
        val bigDiff = diff.copy(path = "Big.kt")
        val repository = FakeGitRepository(diffs = mapOf("main" to listOf(bigDiff)), files = mapOf("Big.kt" to bigContent))
        val client = RecordingClient(
            """{"summary":"1 bug","comments":[{"filePath":"Big.kt","startLine":1,"endLine":1,"severity":"BUG","category":"logic",
               "message":"last item skipped","originalSnippet":"for (i in 0 until xs.size - 1) println(xs[i])"}]}""",
        )

        val nit = LlmReviewAgent(client).review(repository, "main").comments.single()

        assertTrue(bigContent.length > MAX_PROMPT_FILE_CHARS)
        assertEquals(lineOfBug to lineOfBug, nit.startLine to nit.endLine)
    }
}
