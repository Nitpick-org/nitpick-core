package dev.nitpick.app.review

import dev.nitpick.app.domain.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ReviewResponseParserTest {

    @Test
    fun parsesAValidAnswerWrappedInFences() {
        val text = """
            Here is my review:
            ```json
            {"summary": "1 bug", "comments": [
              {"filePath": "a.kt", "startLine": 3, "endLine": 4, "severity": "bug", "category": "logic",
               "message": "off by one", "originalSnippet": "x", "suggestion": "y", "confidence": 0.9}
            ]}
            ```
        """.trimIndent()

        val result = parseReviewResponse(text)

        assertEquals("1 bug", result.summary)
        val comment = result.comments.single()
        assertEquals(Severity.BUG, comment.severity)
        assertEquals(3, comment.startLine)
        assertEquals(4, comment.endLine)
        assertEquals("a.kt:3:0", comment.id)
        assertEquals("y", comment.suggestion)
    }

    @Test
    fun fillsDefaultsForMissingOptionalFields() {
        val comment = parseReviewResponse("""{"comments":[{"filePath":"a.kt","message":"m","severity":"weird"}]}""")
            .comments.single()
        assertEquals(Severity.INFO, comment.severity)
        assertEquals(0, comment.startLine)
        assertEquals(0, comment.endLine)
        assertNull(comment.suggestion)
    }

    @Test
    fun rejectsAnAnswerWithoutJson() {
        assertFailsWith<ReviewResponseException> { parseReviewResponse("I cannot review this.") }
        assertFailsWith<ReviewResponseException> { parseReviewResponse("{\"comments\": [{}]}") }
    }
}
