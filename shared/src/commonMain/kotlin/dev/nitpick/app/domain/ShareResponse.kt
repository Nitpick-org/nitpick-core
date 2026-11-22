package dev.nitpick.app.domain

import kotlinx.serialization.Serializable

/** Answer of `POST /reviews`: the id the server gave the review. */
@Serializable
data class ShareResponse(val id: String)
