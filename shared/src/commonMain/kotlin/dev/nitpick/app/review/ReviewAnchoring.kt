package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewComment

/**
 * Applies a review suggestion without trusting the line numbers blindly. The model may have
 * seen a stale file, so the snippet is checked first, then searched, then the edit is
 * refused instead of corrupting the file.
 */
object ReviewAnchoring {

    fun apply(content: String, comment: ReviewComment): String {
        val suggestion = comment.suggestion
            ?: throw IllegalArgumentException("Comment ${comment.id} has no suggestion")
        val lines = content.split("\n")
        val snippet = comment.originalSnippet.split("\n")
        if (snippet.isEmpty()) throw IllegalArgumentException("Comment ${comment.id} has no snippet")

        val start = findSnippet(lines, snippet, preferredStart = comment.startLine - 1)
            ?: throw IllegalArgumentException("Snippet not found for ${comment.id}")
        val endExclusive = start + snippet.size
        val replacement = suggestion.split("\n")
        return (lines.subList(0, start) + replacement + lines.subList(endExclusive, lines.size))
            .joinToString("\n")
    }

    /** Returns a comment whose line is moved to the found snippet, or null if it disappeared. */
    fun reanchor(content: String, comment: ReviewComment): ReviewComment? {
        val lines = content.split("\n")
        val snippet = comment.originalSnippet.split("\n")
        val start = findSnippet(lines, snippet, preferredStart = comment.startLine - 1) ?: return null
        return comment.copy(startLine = start + 1, endLine = start + snippet.size)
    }

    private fun findSnippet(lines: List<String>, snippet: List<String>, preferredStart: Int): Int? {
        val maxStart = lines.size - snippet.size
        if (maxStart < 0) return null
        if (preferredStart in 0..maxStart && matches(lines, snippet, preferredStart)) return preferredStart
        for (start in 0..maxStart) {
            if (matches(lines, snippet, start)) return start
        }
        return null
    }

    private fun matches(lines: List<String>, snippet: List<String>, start: Int): Boolean =
        snippet.indices.all { index -> lines[start + index] == snippet[index] }
}
