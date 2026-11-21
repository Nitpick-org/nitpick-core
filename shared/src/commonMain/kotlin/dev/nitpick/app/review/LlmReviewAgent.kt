package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.git.GitRepository

/** One prompt, one answer: builds the context, asks [client], parses and re-anchors every nit. */
class LlmReviewAgent(private val client: LlmClient) : ReviewAgent {

    override suspend fun review(repository: GitRepository, baseRef: String): ReviewResult {
        val context = buildReviewContext(repository, baseRef)
        if (context.diffs.isEmpty()) return ReviewResult(summary = "", comments = emptyList())
        val prompt = buildReviewPrompt(context, toolsAvailable = client is ToolCallingLlmClient)
        val answer = if (client is ToolCallingLlmClient) {
            client.completeWithTools(prompt.system, prompt.user, ReviewToolbox(repository))
        } else {
            client.complete(prompt.system, prompt.user)
        }
        val parsed = parseReviewResponse(answer)
        val reviewedPaths = context.diffs.mapTo(HashSet()) { it.path }
        val anchored = parsed.comments
            .filter { it.filePath in reviewedPaths }
            .map { ReviewAnchoring.anchorOrFileLevel(context.files[it.filePath], it) }
            .sortedWith(compareByDescending<ReviewComment> { it.severity }.thenBy { it.filePath }.thenBy { it.startLine })
        return parsed.copy(comments = anchored)
    }
}
