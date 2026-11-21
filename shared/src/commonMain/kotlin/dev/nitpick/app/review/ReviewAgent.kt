package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.git.GitRepository

interface ReviewAgent {
    suspend fun review(repository: GitRepository, baseRef: String): ReviewResult
}
