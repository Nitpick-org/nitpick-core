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

    private class RecordingClient(private val answer: String) : LlmClient {
        var user = ""
        override suspend fun complete(system: String, user: String): String { this.user = user; return answer }
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
    fun anEmptyDiffNeverCallsTheModel() = runTest {
        val client = RecordingClient("not json")
        val result = LlmReviewAgent(client).review(FakeGitRepository(diffs = mapOf("main" to emptyList())), "main")
        assertTrue(result.comments.isEmpty())
        assertEquals("", client.user)
    }
}
