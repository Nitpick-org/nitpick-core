package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewComment
import kotlin.math.abs

/**
 * The single place that decides where a nit's `originalSnippet` lives in a file. Lines are
 * compared trimmed, because models often drop indentation, and the match nearest to the
 * claimed line wins. Applying refuses rather than guessing; anchoring falls back to the
 * whole file so the UI never points at the wrong code.
 */
object ReviewAnchoring {

    fun apply(content: String, comment: ReviewComment): String {
        val suggestion = comment.suggestion
            ?: throw IllegalArgumentException("Comment ${comment.id} has no suggestion")
        val lines = content.split("\n")
        val snippet = normalizedSnippet(comment.originalSnippet)
        if (snippet.isEmpty()) throw IllegalArgumentException("Comment ${comment.id} has no snippet")
        val start = findSnippet(lines, snippet, preferredStart = comment.startLine - 1)
            ?: throw IllegalArgumentException("Snippet not found for ${comment.id}")
        val replacement = suggestion.split("\n")
        return (lines.subList(0, start) + replacement + lines.subList(start + snippet.size, lines.size))
            .joinToString("\n")
    }

    /** The comment moved to where its snippet really is, or null if the snippet is gone. */
    fun reanchor(content: String, comment: ReviewComment): ReviewComment? {
        val snippet = normalizedSnippet(comment.originalSnippet)
        if (snippet.isEmpty()) return null
        val start = findSnippet(content.split("\n"), snippet, preferredStart = comment.startLine - 1) ?: return null
        return comment.copy(startLine = start + 1, endLine = start + snippet.size)
    }

    /** Like [reanchor], but a missing file or snippet gives a whole-file nit instead of nothing. */
    fun anchorOrFileLevel(content: String?, comment: ReviewComment): ReviewComment {
        if (comment.isFileLevel) return comment
        if (content == null) return comment.asFileLevel()
        if (normalizedSnippet(comment.originalSnippet).isEmpty()) {
            val lineCount = content.split("\n").size
            val inRange = comment.startLine in 1..lineCount && comment.endLine in comment.startLine..lineCount
            return if (inRange) comment else comment.asFileLevel()
        }
        return reanchor(content, comment) ?: comment.asFileLevel()
    }

    private fun normalizedSnippet(snippet: String): List<String> =
        snippet.replace("\r\n", "\n").split("\n").map { it.trim() }
            .dropWhile { it.isEmpty() }.dropLastWhile { it.isEmpty() }

    private fun findSnippet(lines: List<String>, snippet: List<String>, preferredStart: Int): Int? {
        val trimmed = lines.map { it.trim() }
        val maxStart = trimmed.size - snippet.size
        if (maxStart < 0) return null
        if (preferredStart in 0..maxStart && matches(trimmed, snippet, preferredStart)) return preferredStart
        return (0..maxStart).filter { matches(trimmed, snippet, it) }.minByOrNull { abs(it - preferredStart) }
    }

    private fun matches(lines: List<String>, snippet: List<String>, start: Int): Boolean =
        snippet.indices.all { index -> lines[start + index] == snippet[index] }

    private fun ReviewComment.asFileLevel() = copy(startLine = 0, endLine = 0)
}
