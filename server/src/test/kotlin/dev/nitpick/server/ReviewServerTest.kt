package dev.nitpick.server

import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.domain.Severity
import dev.nitpick.app.domain.ShareResponse
import dev.nitpick.app.domain.SharedReview
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import io.ktor.server.testing.testApplication
import io.ktor.http.HttpHeaders
import io.ktor.client.request.header
import java.nio.file.Files
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.writeText
import kotlin.io.path.ExperimentalPathApi
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

@OptIn(ExperimentalPathApi::class)
class ReviewServerTest {

    private val directory = Files.createTempDirectory("nitpick-server")
    private val file = directory.resolve("reviews.json")

    @AfterTest
    fun cleanUp() = directory.deleteRecursively()

    private fun review(repoUrl: String = LEDGER, branch: String = "feature/search") = SharedReview(
        repoUrl = repoUrl,
        branch = branch,
        baseRef = "main",
        headCommit = "a27039a",
        result = ReviewResult(
            summary = "One off-by-one in pagination.",
            comments = listOf(
                ReviewComment(
                    id = "c1", filePath = "src/main/kotlin/ledger/ExpenseStore.kt", startLine = 42, endLine = 42,
                    severity = Severity.BUG, category = "correctness", message = "Page 1 skips the first 10 rows.",
                    originalSnippet = "val offset = page * PAGE_SIZE", suggestion = "val offset = (page - 1) * PAGE_SIZE",
                ),
            ),
        ),
    )

    private fun serverTest(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application { reviewModule(ReviewStore(file)) }
        block()
    }

    private suspend fun HttpClient.share(review: SharedReview): HttpResponse = post("/reviews") {
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(review))
    }

    private suspend fun HttpClient.shareId(review: SharedReview): String =
        Json.decodeFromString<ShareResponse>(share(review).bodyAsText()).id

    @Test
    fun aSharedReviewComesBackByIdAndByRepository() = serverTest {
        val response = client.share(review())
        assertEquals(HttpStatusCode.Created, response.status)
        val id = Json.decodeFromString<ShareResponse>(response.bodyAsText()).id

        val byId = Json.decodeFromString<SharedReview>(client.get("/reviews/$id").bodyAsText())
        val byRepo = Json.decodeFromString<List<SharedReview>>(client.get("/reviews") { parameter("repoUrl", LEDGER) }.bodyAsText())

        assertEquals(review().copy(id = id), byId)
        assertEquals(listOf(byId), byRepo)
    }

    @Test
    fun theListKeepsOneRepositoryNewestFirst() = serverTest {
        val first = client.shareId(review(branch = "feature/search"))
        client.shareId(review(repoUrl = "https://github.com/Nitpick-org/nitpick-core"))
        val second = client.shareId(review(branch = "feature/import"))

        val listed = Json.decodeFromString<List<SharedReview>>(client.get("/reviews") { parameter("repoUrl", LEDGER) }.bodyAsText())

        assertEquals(listOf(second, first), listed.map { it.id })
    }

    @Test
    fun anUnknownReviewIsNotFound() = serverTest {
        assertEquals(HttpStatusCode.NotFound, client.get("/reviews/00000000-0000-0000-0000-000000000000").status)
        assertEquals(HttpStatusCode.BadRequest, client.get("/reviews/not-an-id").status)
    }

    @Test
    fun invalidRequestsAreRejected() = serverTest {
        val notJson = client.post("/reviews") { contentType(ContentType.Application.Json); setBody("{not json") }
        val blankRepo = client.share(review(repoUrl = " "))
        val tooLarge = client.post("/reviews") {
            contentType(ContentType.Application.Json)
            setBody("x".repeat(MAX_BODY_BYTES.toInt() + 1))
        }

        assertEquals(HttpStatusCode.BadRequest, notJson.status)
        assertEquals(HttpStatusCode.BadRequest, blankRepo.status)
        assertEquals(HttpStatusCode.PayloadTooLarge, tooLarge.status)
        assertEquals(HttpStatusCode.BadRequest, client.get("/reviews").status)
    }

    @Test
    fun aBodyWithoutContentLengthIsStillLimited() = serverTest {
        val streamed = client.post("/reviews") {
            setBody(object : OutgoingContent.WriteChannelContent() {
                override val contentType = ContentType.Application.Json
                override suspend fun writeTo(channel: ByteWriteChannel) =
                    channel.writeFully(ByteArray(MAX_BODY_BYTES.toInt() + 1) { 'x'.code.toByte() })
            })
        }

        assertEquals(HttpStatusCode.PayloadTooLarge, streamed.status)
    }

    @Test
    fun theLaptopSshRemoteAndThePhoneHttpsCloneShareOneList() = serverTest {
        val id = client.shareId(review(repoUrl = "git@github.com:Nitpick-org/pocket-ledger.git"))

        val listed = Json.decodeFromString<List<SharedReview>>(client.get("/reviews") { parameter("repoUrl", "$LEDGER.git") }.bodyAsText())

        assertEquals(listOf(id), listed.map { it.id })
    }

    @Test
    fun remotesOfOneRepositoryNormalizeToOneKey() {
        listOf(
            "git@github.com:Nitpick-org/pocket-ledger.git",
            "ssh://git@github.com/Nitpick-org/pocket-ledger.git",
            "https://GitHub.com/Nitpick-org/pocket-ledger/",
            "https://user@github.com/Nitpick-org/pocket-ledger.git",
            "github.com:Nitpick-org/pocket-ledger.git",
            "git@github.com:/Nitpick-org/pocket-ledger.git",
            "https://github.com/Nitpick-Org/Pocket-Ledger",
            LEDGER,
        ).forEach { assertEquals(LEDGER, normalizeRepoUrl(it), it) }
        assertEquals("/Users/me/repo", normalizeRepoUrl("/Users/me/repo"))
        // A self-hosted forge keeps its path case and its port: two forges on one machine are two repos.
        assertEquals("https://forge.local:3000/Team/Repo", normalizeRepoUrl("http://forge.local:3000/Team/Repo.git"))
        assertNotEquals(normalizeRepoUrl("http://forge.local:3000/o/r"), normalizeRepoUrl("http://forge.local:4000/o/r"))
    }

    @Test
    fun reviewsSurviveARestart() {
        val saved = ReviewStore(file).add(review())

        assertEquals(saved, ReviewStore(file).get(saved.id))
        assertEquals(listOf(saved), ReviewStore(file).list(LEDGER))
    }

    @Test
    fun aCorruptStoreStartsEmptyAndKeepsTheBadFile() {
        file.writeText("{not json")

        val store = ReviewStore(file)

        assertTrue(store.list(LEDGER).isEmpty())
        assertTrue(file.parent.toFile().listFiles()!!.any { it.name.startsWith(file.name) && it.name.endsWith(".corrupt") })
    }

    @Test
    fun aFieldThisServerDoesNotKnowIsIgnoredNotRejected() = serverTest {
        val body = Json.encodeToString(review()).dropLast(1) + ""","futureField": 1}"""
        val response = client.post("/reviews") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun aLanServerWantsItsTokenOnEveryReviewRoute() = testApplication {
        application { reviewModule(ReviewStore(file), token = "team-secret") }

        assertEquals(HttpStatusCode.Unauthorized, client.share(review()).status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/reviews?repoUrl=$LEDGER").status)
        assertEquals(HttpStatusCode.OK, client.get("/health").status)
        val accepted = client.post("/reviews") {
            header(HttpHeaders.Authorization, "Bearer team-secret")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(review()))
        }
        assertEquals(HttpStatusCode.Created, accepted.status)
    }

    @Test
    fun healthAnswers() = serverTest {
        assertEquals(HttpStatusCode.OK, client.get("/health").status)
    }

    private companion object {
        const val LEDGER = "https://github.com/nitpick-org/pocket-ledger"
    }
}
