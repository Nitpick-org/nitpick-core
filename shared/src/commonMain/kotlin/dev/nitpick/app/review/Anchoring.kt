package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewComment

/**
 * Checks that [comment].originalSnippet really sits at startLine..endLine of [fileContent]. If
 * not, the snippet is searched in the file and the nearest match wins; if it is nowhere, the
 * comment becomes file-level so the UI never points at the wrong code.
 */
fun anchorComment(comment: ReviewComment, fileContent: String?): ReviewComment {
    if (comment.isFileLevel) return comment
    if (fileContent == null) return comment.asFileLevel()
    val lines = fileContent.replace("\r\n", "\n").lines().map { it.trim() }
    val snippet = comment.originalSnippet.replace("\r\n", "\n").lines().map { it.trim() }
        .dropWhile { it.isEmpty() }.dropLastWhile { it.isEmpty() }
    if (snippet.isEmpty()) {
        val inRange = comment.startLine in 1..lines.size && comment.endLine in comment.startLine..lines.size
        return if (inRange) comment else comment.asFileLevel()
    }
    val claimedIndex = comment.startLine - 1
    if (matchesAt(lines, snippet, claimedIndex)) {
        return comment.copy(endLine = comment.startLine + snippet.size - 1)
    }
    val candidates = lines.indices.filter { matchesAt(lines, snippet, it) }
    val nearest = candidates.minByOrNull { kotlin.math.abs(it - claimedIndex) } ?: return comment.asFileLevel()
    return comment.copy(startLine = nearest + 1, endLine = nearest + snippet.size)
}

private fun matchesAt(lines: List<String>, snippet: List<String>, index: Int): Boolean =
    index >= 0 && index + snippet.size <= lines.size &&
        snippet.indices.all { lines[index + it] == snippet[it] }

private fun ReviewComment.asFileLevel() = copy(startLine = 0, endLine = 0)
