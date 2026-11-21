package dev.nitpick.app.domain

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ReviewResultSerializationTest {

    @Test
    fun roundTripsThroughJson() {
        val result = ReviewResult(
            summary = "1 bug, 1 nit",
            comments = listOf(
                ReviewComment(
                    id = "c1",
                    filePath = "src/Main.kt",
                    startLine = 42,
                    endLine = 42,
                    severity = Severity.BUG,
                    category = "logic",
                    message = "Off-by-one: the last element is skipped.",
                    originalSnippet = "for (i in 0 until n - 1)",
                    suggestion = "for (i in 0 until n)",
                ),
                ReviewComment(
                    id = "c2",
                    filePath = "README.md",
                    startLine = 1,
                    endLine = 3,
                    severity = Severity.INFO,
                    category = "readability",
                    message = "Consider a one-line summary.",
                    originalSnippet = "# Title",
                ),
            ),
        )

        val json = Json.encodeToString(ReviewResult.serializer(), result)

        assertEquals(result, Json.decodeFromString(ReviewResult.serializer(), json))
    }
}
