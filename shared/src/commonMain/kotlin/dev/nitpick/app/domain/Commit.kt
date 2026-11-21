package dev.nitpick.app.domain

data class Commit(
    val id: String,
    val shortMessage: String,
    val fullMessage: String,
    val authorName: String,
    val authorEmail: String,
    val timestampEpochSeconds: Long,
    val parentIds: List<String>,
)
