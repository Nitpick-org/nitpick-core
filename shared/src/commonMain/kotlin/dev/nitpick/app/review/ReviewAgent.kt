package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.git.GitRepository

interface ReviewAgent {
    /**
     * Reviews the branch against [baseRef]. [onToolUse] fires once per guarded tool call the
     * model makes, in order — tool name and path only, never file contents or keys (#80).
     */
    suspend fun review(
        repository: GitRepository,
        baseRef: String,
        onToolUse: (ReviewToolUse) -> Unit = {},
    ): ReviewResult
}
