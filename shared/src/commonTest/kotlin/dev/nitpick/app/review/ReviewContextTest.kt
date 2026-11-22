package dev.nitpick.app.review

import dev.nitpick.app.domain.DiffLine
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.FileChangeType
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.Hunk
import dev.nitpick.app.git.FakeGitRepository
import dev.nitpick.app.git.GitRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewContextTest {

    private fun diff(path: String, added: Int, binary: Boolean = false) = FileDiff(
        path = path,
        oldPath = null,
        changeType = FileChangeType.MODIFIED,
        isBinary = binary,
        hunks = if (binary) emptyList() else listOf(
            Hunk(
                oldStart = 1,
                oldCount = 0,
                newStart = 1,
                newCount = added,
                lines = (1..added).map { line ->
                    DiffLine(DiffLineType.ADDED, "line $line", null, line)
                },
            ),
        ),
    )

    @Test
    fun assetSidecarsAreNotReviewable() {
        assertFalse(isReviewable(diff("characters/hero.png.import", added = 2)))
        assertFalse(isReviewable(diff("assets/logo.png", added = 2)))
        assertTrue(isReviewable(diff("src/Hero.kt", added = 2)))
    }

    @Test
    fun anUnreadableChangedFileIsSkippedInsteadOfFailingTheReview() = runTest {
        val link = diff("src/link.kt", added = 2)
        val base = FakeGitRepository(
            branch = "feature",
            diffs = mapOf("main" to listOf(link)),
            files = mapOf("src/link.kt" to "content"),
        )
        val repository = object : GitRepository by base {
            override suspend fun readFile(path: String): String? =
                throw IllegalArgumentException("symlink leaves the work tree")
        }

        val context = buildReviewContext(repository, "main")

        assertTrue(context.diffs.any { it.path == "src/link.kt" })
        assertTrue("src/link.kt" !in context.files)
    }

    @Test
    fun theMostChangedSourceFilesWinOverTheAlphabeticalFirstFiles() = runTest {
        val small = (0 until 20).map { diff("src/a${it.toString().padStart(2, '0')}.kt", added = 1) }
        val large = (0 until 5).map { diff("src/z${it.toString().padStart(2, '0')}.kt", added = 100) }
        val repository = FakeGitRepository(
            branch = "feature",
            diffs = mapOf("main" to small + large),
            files = (small + large).associate { it.path to "content" },
        )

        val context = buildReviewContext(repository, "main")

        val paths = context.diffs.map { it.path }
        assertTrue(paths.containsAll(large.map { it.path }), "large source files must be selected: $paths")
        assertTrue(paths.none { it.endsWith(".import") || it.endsWith(".png") })
    }
}
