package dev.nitpick.app.domain

data class Credentials(
    val username: String,
    val token: String,
)

data class PullResult(
    val updated: Boolean,
    val conflictedPaths: List<String>,
)
