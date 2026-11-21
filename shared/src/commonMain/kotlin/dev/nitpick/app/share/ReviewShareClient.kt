package dev.nitpick.app.share

import dev.nitpick.app.domain.SharedReview

/** The review-sharing server (CONTEXT §7.1): it only ever receives a review already produced on a device. */
interface ReviewShareClient {
    suspend fun share(review: SharedReview): String // returns the id
    suspend fun list(repoUrl: String): List<SharedReview>
    suspend fun get(id: String): SharedReview?
}

/** The server could not be reached or answered with an error; the UI shows it, the app keeps working. */
class ReviewShareException(message: String, cause: Throwable? = null) : Exception(message, cause)

private val USER_INFO = Regex("""^([a-zA-Z][a-zA-Z0-9+.-]*://)[^@/]+@""")

/** `https://token@github.com/o/r` → `https://github.com/o/r`: a token in a remote URL must never reach the server. */
fun withoutCredentials(remoteUrl: String): String = remoteUrl.replace(USER_INFO, "$1")
