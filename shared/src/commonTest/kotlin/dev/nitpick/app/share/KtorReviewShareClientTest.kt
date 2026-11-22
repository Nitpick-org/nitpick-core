package dev.nitpick.app.share

import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.domain.SharedReview
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class KtorReviewShareClientTest {

    private val review = SharedReview(
        repoUrl = "https://github.com/Nitpick-org/pocket-ledger",
        branch = "feature/search-pages-import",
        baseRef = "main",
        headCommit = "cfd2c60",
        result = ReviewResult(summary = "One off-by-one.", comments = emptyList()),
    )
    private val requests = mutableListOf<HttpRequestData>()

    private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        KtorReviewShareClient(
            serverUrl = { "http://laptop:8080/" },
            http = HttpClient(MockEngine { request -> requests += request; handler(request) }),
        )

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    @Test
    fun shareSendsTheReviewAndReturnsTheServerId() = runTest {
        val client = client { json("""{"id":"4cbebbcd"}""", HttpStatusCode.Created) }

        val id = client.share(review.copy(id = "local"))

        assertEquals("4cbebbcd", id)
        val request = requests.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("http://laptop:8080/reviews", request.url.toString())
        // The id is the server's to assign; nothing else, and never a key, goes in the body.
        assertEquals(review, Json.decodeFromString<SharedReview>(request.body.toByteArray().decodeToString()))
    }

    @Test
    fun listAsksForTheRepository() = runTest {
        val shared = review.copy(id = "4cbebbcd")
        val client = client { json(Json.encodeToString(listOf(shared))) }

        assertEquals(listOf(shared), client.list(review.repoUrl))
        assertEquals(review.repoUrl, requests.single().url.parameters["repoUrl"])
    }

    @Test
    fun anUnknownIdIsNull() = runTest {
        val client = client { respond("", HttpStatusCode.NotFound) }

        assertNull(client.get("00000000-0000-0000-0000-000000000000"))
    }

    @Test
    fun anUnreachableOrFailingServerIsOneCleanError() = runTest {
        val refused = client { throw IllegalStateException("Connection refused") }
        val broken = client { respond("boom", HttpStatusCode.InternalServerError) }

        assertFailsWith<ReviewShareException> { refused.share(review) }
        assertFailsWith<ReviewShareException> { broken.list(review.repoUrl) }
    }

    @Test
    fun aRefusedRequestKeepsItsStatusAndAnUnreachableServerHasNone() = runTest {
        val tooLarge = client { respond("too large", HttpStatusCode.PayloadTooLarge) }
        val refused = client { throw IllegalStateException("Connection refused") }

        assertEquals(413, assertFailsWith<ReviewShareException> { tooLarge.share(review) }.status)
        assertEquals(null, assertFailsWith<ReviewShareException> { refused.share(review) }.status)
    }

    @Test
    fun theServerTokenTravelsAsABearerHeaderOnlyWhenSet() = runTest {
        val withToken = KtorReviewShareClient(
            serverUrl = { "http://laptop:8080" },
            serverToken = { "team-secret" },
            http = HttpClient(MockEngine { request -> requests += request; json("""[]""") }),
        )
        withToken.list(review.repoUrl)
        client { json("""[]""") }.list(review.repoUrl)

        assertEquals("Bearer team-secret", requests[0].headers["Authorization"])
        assertEquals(null, requests[1].headers["Authorization"])
    }

    @Test
    fun aTokenInTheRemoteUrlNeverLeavesTheDevice() {
        assertEquals("https://github.com/o/r.git", withoutCredentials("https://ghp_secret@github.com/o/r.git"))
        assertEquals("https://github.com/o/r", withoutCredentials("https://x-access-token:ghp_secret@github.com/o/r"))
        assertEquals("git@github.com:o/r.git", withoutCredentials("git@github.com:o/r.git"))
        assertEquals("https://gitlab.example.com/o/r", withoutCredentials("https://gitlab.example.com/o/r?private_token=abc"))
    }
}
