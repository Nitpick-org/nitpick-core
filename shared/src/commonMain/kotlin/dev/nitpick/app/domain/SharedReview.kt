package dev.nitpick.app.domain

import kotlinx.serialization.Serializable

/** A review sent to the sharing server (docs/CONTEXT.md §7.1); [repoUrl] identifies the repo on every device. */
@Serializable
data class SharedReview(
    val id: String = "", // assigned by the server, so a share request leaves it empty
    val repoUrl: String,
    val branch: String,
    val baseRef: String,
    val headCommit: String,
    val result: ReviewResult,
)
