package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnchoringTest {

    private val file = "a\nb\nc\nd\ne\n"

    private fun comment(start: Int, end: Int, snippet: String) = ReviewComment(
        id = "c", filePath = "f.kt", startLine = start, endLine = end, severity = Severity.INFO,
        category = "other", message = "m", originalSnippet = snippet,
    )

    @Test
    fun keepsACorrectlyAnchoredComment() {
        val anchored = anchorComment(comment(2, 3, "b\nc"), file)
        assertEquals(2 to 3, anchored.startLine to anchored.endLine)
    }

    @Test
    fun movesAShiftedSnippetToWhereItReallyIs() {
        val anchored = anchorComment(comment(1, 2, "  c\nd  "), file)
        assertEquals(3 to 4, anchored.startLine to anchored.endLine)
    }

    @Test
    fun fallsBackToTheWholeFileWhenTheSnippetIsNotThere() {
        val anchored = anchorComment(comment(2, 2, "zzz"), file)
        assertTrue(anchored.isFileLevel)
    }

    @Test
    fun fallsBackToTheWholeFileWhenTheFileIsUnknown() {
        assertTrue(anchorComment(comment(2, 2, "b"), null).isFileLevel)
    }
}
