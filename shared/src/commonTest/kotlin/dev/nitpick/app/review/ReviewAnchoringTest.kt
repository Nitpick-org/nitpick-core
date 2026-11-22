package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @Test
    fun appliesASnippetTheModelReturnedWithoutIndentation() {
        val content = "fun f() {\n    val x = 1\n}\n"
        val updated = ReviewAnchoring.apply(content, comment(2, "val x = 1", "    val x = 2"))

        assertEquals("fun f() {\n    val x = 2\n}\n", updated)
    }

    @Test
    fun givesAnUnindentedSuggestionTheOriginalLineIndentation() {
        val content = "fun f() {\n    val x = 1\n}\n"
        val updated = ReviewAnchoring.apply(content, comment(2, "val x = 1", "val x = 2"))

        assertEquals("fun f() {\n    val x = 2\n}\n", updated)
    }

    @Test
    fun keepsTheRelativeIndentationOfAMultiLineSuggestion() {
        val content = "    if (x) {\n        y()\n    }\n"
        val snippet = "if (x) {\n        y()\n    }"
        val updated = ReviewAnchoring.apply(content, comment(1, snippet, "if (x) {\n    y()\n}"))

        assertEquals("    if (x) {\n        y()\n    }\n", updated)
    }

    @Test
    fun picksTheOccurrenceNearestToTheClaimedLine() {
        val content = "a\n}\nb\nc\n}\n"
        val updated = ReviewAnchoring.apply(content, comment(4, "}", "end"))

        assertEquals("a\n}\nb\nc\nend\n", updated)
    }

    @Test
    fun anchorOrFileLevelMovesAShiftedSnippetAndFallsBackToTheWholeFile() {
        val moved = ReviewAnchoring.anchorOrFileLevel("zero\none\ntwo\n", comment(1, "two", "x"))
        assertEquals(3 to 3, moved.startLine to moved.endLine)

        assertTrue(ReviewAnchoring.anchorOrFileLevel("one\n", comment(1, "zzz", "x")).isFileLevel)
        assertTrue(ReviewAnchoring.anchorOrFileLevel(null, comment(1, "one", "x")).isFileLevel)
        assertTrue(ReviewAnchoring.anchorOrFileLevel("one\n", comment(9, "", "x")).isFileLevel)
    }
}
