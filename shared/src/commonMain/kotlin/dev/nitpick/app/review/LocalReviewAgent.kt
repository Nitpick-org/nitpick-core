package dev.nitpick.app.review

import dev.nitpick.app.domain.DiffLine
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.domain.Severity
import dev.nitpick.app.git.GitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Deterministic fallback used until the real model path is wired. It reviews added lines
 * with a few high-signal rules (debug output, TODOs, unsafe null assertions, very long
 * lines) so the whole apply/commit loop can be built and demoed without a provider key.
 *
 * This is not the product AI: `KoogReviewAgent` (#20) replaces it behind the same
 * [ReviewAgent] contract.
 */
class LocalReviewAgent : ReviewAgent {
    override suspend fun review(repository: GitRepository, baseRef: String): ReviewResult =
        withContext(Dispatchers.Default) {
            val diff = repository.diffAgainst(baseRef)
            val comments = buildList {
                diff.forEach { file ->
                    file.hunks.forEach { hunk ->
                        hunk.lines.filter { it.type == DiffLineType.ADDED }.forEach { line ->
                            commentFor(file.path, line)?.let(::add)
                        }
                    }
                }
            }.sortedWith(compareByDescending<ReviewComment> { it.severity }.thenBy { it.filePath }.thenBy { it.startLine })
            val bugs = comments.count { it.severity == Severity.BUG }
            ReviewResult(
                summary = "${comments.size} nits · $bugs bug",
                comments = comments,
            )
        }

    private fun commentFor(path: String, line: DiffLine): ReviewComment? {
        val number = line.newLineNumber ?: return null
        val content = line.content
        val (severity, category, message, suggestion) = when {
            "TODO" in content || "FIXME" in content ->
                Rule(Severity.WARNING, "maintainability", "This TODO/FIXME would ship with the branch.")
            "println(" in content || "print(" in content ->
                Rule(Severity.SUGGESTION, "debug", "Debug output left in the branch.", "// $content")
            "!!" in content ->
                Rule(Severity.BUG, "null-safety", "The !! can crash on a null value; prefer an explicit error or safe call.")
            content.length > 120 ->
                Rule(Severity.INFO, "readability", "This line is longer than 120 characters.")
            else -> return null
        }
        return ReviewComment(
            id = "local:${path}:$number:${category}",
            filePath = path,
            startLine = number,
            endLine = number,
            severity = severity,
            category = category,
            message = message,
            originalSnippet = content,
            suggestion = suggestion,
        )
    }

    private data class Rule(
        val severity: Severity,
        val category: String,
        val message: String,
        val suggestion: String? = null,
    )
}
