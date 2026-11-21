package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.domain.Severity
import dev.nitpick.app.git.GitRepository

private val SEVERITY_ORDER = listOf(Severity.BUG, Severity.WARNING, Severity.SUGGESTION, Severity.INFO)

/** One prompt, one answer: builds the context, asks [client], parses and re-anchors every nit. */
class LlmReviewAgent(private val client: LlmClient) : ReviewAgent {

    override suspend fun review(repository: GitRepository, baseRef: String): ReviewResult {
        val context = buildReviewContext(repository, baseRef)
        if (context.diffs.isEmpty()) return ReviewResult(summary = "", comments = emptyList())
        val prompt = buildReviewPrompt(context)
        val answer = client.complete(prompt.system, prompt.user)
        val parsed = parseReviewResponse(answer)
        val reviewedPaths = context.diffs.mapTo(HashSet()) { it.path }
        val anchored = parsed.comments
            .filter { it.filePath in reviewedPaths }
            .map { anchorComment(it, context.files[it.filePath]) }
            .sortedWith(compareBy({ SEVERITY_ORDER.indexOf(it.severity) }, { it.filePath }, { it.startLine }))
        return parsed.copy(comments = anchored)
    }
}
