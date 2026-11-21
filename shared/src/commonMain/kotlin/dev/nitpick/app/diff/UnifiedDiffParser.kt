package dev.nitpick.app.diff

import dev.nitpick.app.domain.DiffLine
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.FileChangeType
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.Hunk

private const val FILE_HEADER = "diff --git "
private const val DEV_NULL = "/dev/null"
private val HUNK_HEADER = Regex("""^@@ -(\d+)(?:,(\d+))? \+(\d+)(?:,(\d+))? @@""")

fun parseUnifiedDiff(text: String): List<FileDiff> =
    splitFileSections(text.lines()).map(::parseFileSection)

private fun splitFileSections(lines: List<String>): List<List<String>> {
    val sections = mutableListOf<MutableList<String>>()
    for (line in lines) {
        if (line.startsWith(FILE_HEADER)) sections.add(mutableListOf())
        sections.lastOrNull()?.add(line)
    }
    return sections
}

private fun parseFileSection(lines: List<String>): FileDiff {
    val (headerOld, headerNew) = splitHeaderPaths(lines.first().removePrefix(FILE_HEADER))
    val oldPath = lines.firstOrNull { it.startsWith("--- ") }?.let(::markerPath) ?: headerOld
    val newPath = lines.firstOrNull { it.startsWith("+++ ") }?.let(::markerPath) ?: headerNew
    val changeType = when {
        lines.any { it.startsWith("new file mode") } -> FileChangeType.ADDED
        lines.any { it.startsWith("deleted file mode") } -> FileChangeType.DELETED
        lines.any { it.startsWith("rename from ") } -> FileChangeType.RENAMED
        else -> FileChangeType.MODIFIED
    }
    return FileDiff(
        path = newPath ?: oldPath ?: headerNew,
        oldPath = if (changeType == FileChangeType.RENAMED) oldPath else null,
        changeType = changeType,
        isBinary = lines.any { it.startsWith("Binary files ") || it == "GIT binary patch" },
        hunks = parseHunks(lines),
    )
}

// shortcut: paths containing " b/" or quotes are split wrongly, use the ---/+++ lines (git quotes them) if it bites.
private fun splitHeaderPaths(header: String): Pair<String, String> {
    val separator = header.lastIndexOf(" b/")
    if (separator < 0) return header to header
    return header.substring(0, separator).removePrefix("a/") to header.substring(separator + 3)
}

private fun markerPath(line: String): String? {
    val path = line.substring(4).substringBefore('\t')
    if (path == DEV_NULL) return null
    return path.removePrefix("a/").removePrefix("b/")
}

private fun parseHunks(lines: List<String>): List<Hunk> {
    val hunks = mutableListOf<Hunk>()
    var index = 0
    while (index < lines.size) {
        val header = HUNK_HEADER.find(lines[index])
        if (header == null) {
            index++
            continue
        }
        val (hunk, consumed) = parseHunk(header, lines, index + 1)
        hunks.add(hunk)
        index += 1 + consumed
    }
    return hunks
}

private fun parseHunk(header: MatchResult, lines: List<String>, start: Int): Pair<Hunk, Int> {
    val groups = header.groupValues
    val oldStart = groups[1].toInt()
    val oldCount = groups[2].ifEmpty { "1" }.toInt()
    val newStart = groups[3].toInt()
    val newCount = groups[4].ifEmpty { "1" }.toInt()
    val cursor = LineCursor(oldStart, newStart, oldCount, newCount)
    val parsed = mutableListOf<DiffLine>()
    var index = start
    while (index < lines.size && !cursor.isDone()) {
        val line = lines[index]
        if (line.startsWith("@@") || line.startsWith(FILE_HEADER)) break
        index++
        if (line.startsWith("\\")) continue
        parsed.add(cursor.next(line) ?: break)
    }
    return Hunk(oldStart, oldCount, newStart, newCount, parsed) to (index - start)
}

private class LineCursor(
    private var oldLine: Int,
    private var newLine: Int,
    private var oldRemaining: Int,
    private var newRemaining: Int,
) {
    fun isDone(): Boolean = oldRemaining <= 0 && newRemaining <= 0

    // An empty line is a context line whose trailing space was stripped by some tool.
    fun next(line: String): DiffLine? = when {
        line.startsWith("+") -> DiffLine(DiffLineType.ADDED, line.drop(1), null, newLine++).also { newRemaining-- }
        line.startsWith("-") -> DiffLine(DiffLineType.REMOVED, line.drop(1), oldLine++, null).also { oldRemaining-- }
        line.startsWith(" ") || line.isEmpty() -> DiffLine(DiffLineType.CONTEXT, line.drop(1), oldLine++, newLine++)
            .also { oldRemaining--; newRemaining-- }
        else -> null
    }
}
