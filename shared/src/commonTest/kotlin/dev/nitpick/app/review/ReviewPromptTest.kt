package dev.nitpick.app.review

import dev.nitpick.app.domain.DiffLine
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.FileChangeType
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.Hunk
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewPromptTest {

    private val diff = FileDiff(
        path = "src/Main.kt",
        oldPath = null,
        changeType = FileChangeType.MODIFIED,
        isBinary = false,
        hunks = listOf(
            Hunk(
                oldStart = 40, oldCount = 3, newStart = 40, newCount = 3,
                lines = listOf(
                    DiffLine(DiffLineType.CONTEXT, "fun main() {", 40, 40),
                    DiffLine(DiffLineType.REMOVED, "    for (i in 0 until n - 1)", 41, null),
                    DiffLine(DiffLineType.ADDED, "    for (i in 0 until n)", null, 41),
                    DiffLine(DiffLineType.CONTEXT, "}", 42, 42),
                ),
            ),
        ),
    )

    private val context = ReviewContext(
        baseRef = "main", branch = "feature", diffs = listOf(diff),
        files = mapOf("src/Main.kt" to "fun main() {\n    for (i in 0 until n)\n}"),
        fileTree = listOf("src/Main.kt"), readme = "# Demo", recentCommits = listOf("fix loop"), language = "Kotlin",
    )

    @Test
    fun numbersAddedAndContextLinesWithTheNewFileNumber() {
        val user = buildReviewPrompt(context).user
        assertContains(user, "   40 |  fun main() {")
        assertContains(user, "   41 | +    for (i in 0 until n)")
        assertContains(user, "      | -    for (i in 0 until n - 1)")
    }

    @Test
    fun includesContextAndTheNumberedFullFile() {
        val user = buildReviewPrompt(context).user
        assertContains(user, "Main language: Kotlin.")
        assertContains(user, "- fix loop")
        assertContains(user, "# Demo")
        assertContains(user, "    2 |     for (i in 0 until n)")
        assertTrue(buildReviewPrompt(context).system.contains("originalSnippet"))
    }

    @Test
    fun lockfilesBinariesAndBuildOutputAreNotReviewable() {
        fun diffAt(path: String, binary: Boolean = false) = diff.copy(path = path, isBinary = binary)
        assertFalse(isReviewable(diffAt("package-lock.json")))
        assertFalse(isReviewable(diffAt("app/build/out.kt")))
        assertFalse(isReviewable(diffAt("logo.png", binary = true)))
        assertFalse(isReviewable(diffAt("web/app.min.js")))
        assertTrue(isReviewable(diffAt("src/Build.kt")))
    }

    @Test
    fun fullFilesStopAtTheBudgetButTheDiffAlwaysGoesIn() {
        val big = "x".repeat(MAX_PROMPT_FILE_CHARS + 1)
        val chunk = "y".repeat(MAX_PROMPT_TOTAL_CHARS / 8)
        val files = linkedMapOf("big.kt" to big) + (1..9).associate { "f$it.kt" to chunk }
        val printable = filesWithinBudget(files)
        assertEquals((1..8).map { "f$it.kt" }.toSet(), printable.keys)

        val user = buildReviewPrompt(context.copy(files = mapOf("src/Main.kt" to big))).user
        assertContains(user, "   41 | +    for (i in 0 until n)")
        assertFalse(user.contains("## Full content"))
    }
}
