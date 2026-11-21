package dev.nitpick.app.diff

import dev.nitpick.app.domain.DiffLine
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.FileChangeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UnifiedDiffParserTest {

    private val modifiedTwoHunks = """
        |diff --git a/src/Main.kt b/src/Main.kt
        |index 1111111..2222222 100644
        |--- a/src/Main.kt
        |+++ b/src/Main.kt
        |@@ -1,4 +1,5 @@
        | a
        |-b
        |+B
        |+C
        | c
        | d
        |@@ -10,3 +11,3 @@ fun main() {
        | x
        |-y
        |+Y
        | z
        |""".trimMargin()

    private val addedFile = """
        |diff --git a/README.md b/README.md
        |new file mode 100644
        |index 0000000..3333333
        |--- /dev/null
        |+++ b/README.md
        |@@ -0,0 +1,2 @@
        |+# Nitpick
        |+Nitpick your branch before main does.
        |""".trimMargin()

    private val deletedFile = """
        |diff --git a/old.txt b/old.txt
        |deleted file mode 100644
        |index 4444444..0000000
        |--- a/old.txt
        |+++ /dev/null
        |@@ -1,2 +0,0 @@
        |-gone
        |-forever
        |""".trimMargin()

    private val renamedFile = """
        |diff --git a/src/Old.kt b/src/New.kt
        |similarity index 90%
        |rename from src/Old.kt
        |rename to src/New.kt
        |index 5555555..6666666 100644
        |--- a/src/Old.kt
        |+++ b/src/New.kt
        |@@ -1,2 +1,2 @@
        |-class Old
        |+class New
        | val keep = 1
        |""".trimMargin()

    private val binaryFile = """
        |diff --git a/icon.png b/icon.png
        |index 7777777..8888888 100644
        |Binary files a/icon.png and b/icon.png differ
        |""".trimMargin()

    private val noNewlineAtEnd = """
        |diff --git a/a.txt b/a.txt
        |--- a/a.txt
        |+++ b/a.txt
        |@@ -1 +1 @@
        |-hello
        |\ No newline at end of file
        |+hello world
        |\ No newline at end of file
        |""".trimMargin()

    @Test
    fun modifiedFileWithTwoHunksHasCorrectLineNumbers() {
        val diff = parseUnifiedDiff(modifiedTwoHunks).single()

        assertEquals("src/Main.kt", diff.path)
        assertNull(diff.oldPath)
        assertEquals(FileChangeType.MODIFIED, diff.changeType)
        assertEquals(2, diff.hunks.size)
        assertEquals(
            listOf(
                DiffLine(DiffLineType.CONTEXT, "a", 1, 1),
                DiffLine(DiffLineType.REMOVED, "b", 2, null),
                DiffLine(DiffLineType.ADDED, "B", null, 2),
                DiffLine(DiffLineType.ADDED, "C", null, 3),
                DiffLine(DiffLineType.CONTEXT, "c", 3, 4),
                DiffLine(DiffLineType.CONTEXT, "d", 4, 5),
            ),
            diff.hunks[0].lines,
        )
        val second = diff.hunks[1]
        assertEquals(10, second.oldStart)
        assertEquals(11, second.newStart)
        assertEquals(DiffLine(DiffLineType.ADDED, "Y", null, 12), second.lines[2])
        assertEquals(DiffLine(DiffLineType.CONTEXT, "z", 12, 13), second.lines[3])
    }

    @Test
    fun addedFileNumbersOnlyTheNewSide() {
        val diff = parseUnifiedDiff(addedFile).single()

        assertEquals("README.md", diff.path)
        assertEquals(FileChangeType.ADDED, diff.changeType)
        assertEquals(listOf(1, 2), diff.hunks.single().lines.map { it.newLineNumber })
        assertTrue(diff.hunks.single().lines.all { it.oldLineNumber == null })
    }

    @Test
    fun deletedFileKeepsItsOldPath() {
        val diff = parseUnifiedDiff(deletedFile).single()

        assertEquals("old.txt", diff.path)
        assertEquals(FileChangeType.DELETED, diff.changeType)
        assertEquals(listOf(1, 2), diff.hunks.single().lines.map { it.oldLineNumber })
    }

    @Test
    fun renamedFileExposesBothPaths() {
        val diff = parseUnifiedDiff(renamedFile).single()

        assertEquals("src/New.kt", diff.path)
        assertEquals("src/Old.kt", diff.oldPath)
        assertEquals(FileChangeType.RENAMED, diff.changeType)
        assertEquals(DiffLine(DiffLineType.CONTEXT, "val keep = 1", 2, 2), diff.hunks.single().lines.last())
    }

    @Test
    fun binaryFileHasNoHunks() {
        val diff = parseUnifiedDiff(binaryFile).single()

        assertEquals("icon.png", diff.path)
        assertTrue(diff.isBinary)
        assertTrue(diff.hunks.isEmpty())
    }

    @Test
    fun jgitBinaryWordingWithoutPathLinesIsStillBinary() {
        val jgitBinary = """
            |diff --git a/icon.png b/icon.png
            |index 7777777..8888888 100644
            |Binary files differ
            |""".trimMargin()

        val diff = parseUnifiedDiff(jgitBinary).single()

        assertEquals("icon.png", diff.path)
        assertTrue(diff.isBinary)
        assertTrue(diff.hunks.isEmpty())
    }

    @Test
    fun noNewlineMarkerIsSkippedAndCountsDefaultToOne() {
        val hunk = parseUnifiedDiff(noNewlineAtEnd).single().hunks.single()

        assertEquals(1, hunk.oldCount)
        assertEquals(1, hunk.newCount)
        assertEquals(
            listOf(
                DiffLine(DiffLineType.REMOVED, "hello", 1, null),
                DiffLine(DiffLineType.ADDED, "hello world", null, 1),
            ),
            hunk.lines,
        )
    }

    @Test
    fun severalFilesInOneDiffAreSplit() {
        val diffs = parseUnifiedDiff(modifiedTwoHunks + "\n" + addedFile + "\n" + binaryFile)

        assertEquals(listOf("src/Main.kt", "README.md", "icon.png"), diffs.map { it.path })
    }

    @Test
    fun malformedDiffReturnsWhatItUnderstood() {
        val truncatedHunk = """
            |diff --git a/t.txt b/t.txt
            |--- a/t.txt
            |+++ b/t.txt
            |@@ -1,5 +1,5 @@
            | only
            |-one
            |garbage line without prefix
            |""".trimMargin()

        val diff = parseUnifiedDiff(truncatedHunk).single()

        assertEquals(2, diff.hunks.single().lines.size)
        assertTrue(parseUnifiedDiff("not a diff at all").isEmpty())
        assertTrue(parseUnifiedDiff("").isEmpty())
    }
}
