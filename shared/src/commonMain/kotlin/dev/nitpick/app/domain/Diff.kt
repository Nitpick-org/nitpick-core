package dev.nitpick.app.domain

enum class FileChangeType { ADDED, MODIFIED, DELETED, RENAMED }

data class FileDiff(
    val path: String,
    val oldPath: String?,
    val changeType: FileChangeType,
    val isBinary: Boolean,
    val hunks: List<Hunk>,
)

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
