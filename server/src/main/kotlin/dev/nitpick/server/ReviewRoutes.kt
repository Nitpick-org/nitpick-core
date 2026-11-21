package dev.nitpick.server

import dev.nitpick.app.domain.SharedReview
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.contentLength
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

const val MAX_BODY_BYTES = 1_000_000L
private const val MAX_REPO_URL_LENGTH = 2_048
private val REVIEW_ID = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

@Serializable
data class ShareResponse(val id: String)

/** The review-sharing API of docs/CONTEXT.md §7.1. Logs nothing: review content never leaves the store. */
fun Application.reviewModule(store: ReviewStore) {
    install(ContentNegotiation) { json() }
    routing {
        get("/health") { call.respondText("ok") }

        post("/reviews") {
            val review = call.receiveReview() ?: return@post
            if (!isValidRepoUrl(review.repoUrl)) return@post call.respond(HttpStatusCode.BadRequest, "Invalid repoUrl")
            call.respond(HttpStatusCode.Created, ShareResponse(store.add(review).id))
        }

        get("/reviews") {
            val repoUrl = call.request.queryParameters["repoUrl"]
            if (repoUrl == null || !isValidRepoUrl(repoUrl)) return@get call.respond(HttpStatusCode.BadRequest, "Invalid repoUrl")
            call.respond(store.list(repoUrl))
        }

        get("/reviews/{id}") {
            val id = call.parameters["id"].orEmpty()
            if (!REVIEW_ID.matches(id)) return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")
            val review = store.get(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(review)
        }
    }
}

/** Reads at most [MAX_BODY_BYTES], also when no Content-Length is sent; answers the error itself and returns null. */
private suspend fun RoutingCall.receiveReview(): SharedReview? {
    if ((request.contentLength() ?: 0) > MAX_BODY_BYTES) return null.also { respond(HttpStatusCode.PayloadTooLarge) }
    val body = receiveChannel().readRemaining(MAX_BODY_BYTES + 1).readByteArray()
    if (body.size > MAX_BODY_BYTES) return null.also { respond(HttpStatusCode.PayloadTooLarge) }
    return try {
        Json.decodeFromString<SharedReview>(body.decodeToString())
    } catch (_: SerializationException) {
        null.also { respond(HttpStatusCode.BadRequest, "Invalid review JSON") }
    } catch (_: IllegalArgumentException) {
        null.also { respond(HttpStatusCode.BadRequest, "Invalid review JSON") }
    }
}

private fun isValidRepoUrl(repoUrl: String): Boolean =
    repoUrl.isNotBlank() && repoUrl.length <= MAX_REPO_URL_LENGTH && repoUrl.none { it.isWhitespace() || it.isISOControl() }
