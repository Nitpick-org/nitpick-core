package dev.nitpick.app.domain

enum class FileChangeType { ADDED, MODIFIED, DELETED, RENAMED }

data class FileDiff(
    val path: String,
    val oldPath: String?,
    val changeType: FileChangeType,
    val isBinary: Boolean,
    val hunks: List<Hunk>,
) {
    // Counted once when the parser builds the diff: Compose used to rescan every line of every
    // file on each recomposition of the diff panel.
    val addedLines: Int = hunks.sumOf { hunk -> hunk.lines.count { it.type == DiffLineType.ADDED } }
    val removedLines: Int = hunks.sumOf { hunk -> hunk.lines.count { it.type == DiffLineType.REMOVED } }
}

data class Hunk(
    val oldStart: Int,
    val oldCount: Int,
    val newStart: Int,
    val newCount: Int,
    val lines: List<DiffLine>,
)

enum class DiffLineType { CONTEXT, ADDED, REMOVED }

data class DiffLine(
    val type: DiffLineType,
    val content: String,
    val oldLineNumber: Int?,
    val newLineNumber: Int?,
)
