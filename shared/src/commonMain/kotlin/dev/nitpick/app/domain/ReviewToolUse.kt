package dev.nitpick.app.domain

import kotlinx.serialization.Serializable

/** A read-only tool call made by the review agent; no file body or credential is stored. */
@Serializable
data class ReviewToolUse(
    val tool: String,
    val path: String? = null,
)
