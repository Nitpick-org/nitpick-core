package dev.nitpick.app.domain

import kotlinx.serialization.Serializable

enum class Severity { INFO, SUGGESTION, WARNING, BUG }

@Serializable
data class ReviewResult(
    val summary: String,
    val comments: List<ReviewComment>,
    val toolUses: List<ReviewToolUse> = emptyList(),
)

@Serializable
data class ReviewComment(
    val id: String,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val severity: Severity,
    val category: String,
    val message: String,
    val originalSnippet: String,
    val suggestion: String? = null,
    // The fix in a few words, for the commit message; empty when the model gave none.
    val title: String = "",
) {
    /** A nit the reviewer could not anchor to lines: startLine = endLine = 0 means the whole file. */
    val isFileLevel: Boolean get() = startLine == 0 && endLine == 0
}
