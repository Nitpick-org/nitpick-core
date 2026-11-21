package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ReviewAnchoringTest {

    private fun comment(start: Int, snippet: String, suggestion: String) = ReviewComment(
        id = "c1",
        filePath = "src/Main.kt",
        startLine = start,
        endLine = start + snippet.lines().size - 1,
        severity = Severity.SUGGESTION,
        category = "readability",
        message = "message",
        originalSnippet = snippet,
        suggestion = suggestion,
    )

    @Test
    fun replacesTheExactSnippetAtTheGivenLine() {
        val content = "one\ntwo\nthree\n"
        val updated = ReviewAnchoring.apply(content, comment(2, "two", "TWO"))

        assertEquals("one\nTWO\nthree\n", updated)
    }

    @Test
    fun followsTheSnippetWhenTheLineNumberMoved() {
        val content = "zero\none\ntwo\nthree\n"
        val updated = ReviewAnchoring.apply(content, comment(2, "two", "TWO"))

        assertEquals("zero\none\nTWO\nthree\n", updated)
    }

    @Test
    fun refusesWhenTheSnippetIsGone() {
        val content = "one\nthree\n"
        assertFailsWith<IllegalArgumentException> {
            ReviewAnchoring.apply(content, comment(2, "two", "TWO"))
        }
    }

    @Test
    fun reanchorReturnsNullWhenTheSnippetIsGone() {
        assertNull(ReviewAnchoring.reanchor("one\nthree\n", comment(2, "two", "TWO")))
    }
}
