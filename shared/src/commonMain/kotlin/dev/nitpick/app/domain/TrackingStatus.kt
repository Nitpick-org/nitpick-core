package dev.nitpick.app.domain

/** How far the current branch is from its upstream; null when the branch has no upstream. */
data class TrackingStatus(
    val ahead: Int,
    val behind: Int,
    val upstream: String,
)
