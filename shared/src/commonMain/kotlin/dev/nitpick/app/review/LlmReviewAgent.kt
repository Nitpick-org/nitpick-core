package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.domain.ReviewToolUse
import dev.nitpick.app.git.GitRepository

/** One prompt, one answer: builds the context, asks [client], parses and re-anchors every nit. */
class LlmReviewAgent(
    private val client: LlmClient,
    private val onToolUse: (ReviewToolUse) -> Unit = {},
) : ReviewAgent {

    override suspend fun review(repository: GitRepository, baseRef: String): ReviewResult {
        val context = buildReviewContext(repository, baseRef)
        if (context.diffs.isEmpty()) return ReviewResult(summary = "", comments = emptyList())
        val toolsAvailable = client is ToolCallingLlmClient
        val prompt = buildReviewPrompt(context, toolsAvailable = toolsAvailable)
        val usedTools = mutableListOf<dev.nitpick.app.domain.ReviewToolUse>()
        val answer = when (client) {
            is ToolCallingLlmClient -> client.completeWithTools(
                prompt,
                ReviewToolbox(repository, onUse = {
                    usedTools += it
                    onToolUse(it)
                }),
            )
            else -> client.complete(prompt.system, prompt.user)
        }
        val parsed = try {
            parseReviewResponse(answer)
        } catch (e: ReviewResponseException) {
            // Tool-using models sometimes answer in prose after the tool round. Ask once more,
            // without tools, for the JSON contract instead of failing the whole review.
            val repaired = client.complete(
                prompt.withoutTools().system,
                prompt.user + "\n\nYour previous answer did not contain a JSON object. " +
                    "Return ONLY the review JSON object now, with no prose and no markdown fences.",
            )
            parseReviewResponse(repaired)
        }
        val reviewedPaths = context.diffs.mapTo(HashSet()) { it.path }
        val anchored = parsed.comments
            .filter { it.filePath in reviewedPaths }
            .map { ReviewAnchoring.anchorOrFileLevel(context.files[it.filePath], it) }
            .sortedWith(compareByDescending<ReviewComment> { it.severity }.thenBy { it.filePath }.thenBy { it.startLine })
        return parsed.copy(comments = anchored, toolUses = usedTools)
    }
}
