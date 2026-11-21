package dev.nitpick.app.share

import dev.nitpick.app.domain.SharedReview
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ShareResponse(val id: String)

private val json = Json { ignoreUnknownKeys = true }

/**
 * [ReviewShareClient] over HTTP. [serverUrl] is read on every call, so a URL changed in the
 * settings applies at once. `HttpClient()` picks the CIO engine that jvmSharedMain puts on the
 * classpath; tests pass a client on Ktor's MockEngine.
 */
class KtorReviewShareClient(
    private val serverUrl: suspend () -> String,
    private val http: HttpClient = HttpClient {
        // A wrong LAN IP must fail within seconds, not hang the Share button.
        install(HttpTimeout) {
            connectTimeoutMillis = 3_000
            requestTimeoutMillis = 10_000
        }
    },
) : ReviewShareClient {

    override suspend fun share(review: SharedReview): String = call {
        val response = http.post("${base()}/reviews") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(review.copy(id = "")))
        }
        json.decodeFromString<ShareResponse>(response.successBody()).id
    }

    override suspend fun list(repoUrl: String): List<SharedReview> = call {
        json.decodeFromString(http.get("${base()}/reviews") { parameter("repoUrl", repoUrl) }.successBody())
    }

    override suspend fun get(id: String): SharedReview? = call {
        val response = http.get("${base()}/reviews/${id.encodeURLPathPart()}")
        if (response.status == HttpStatusCode.NotFound) null else json.decodeFromString(response.successBody())
    }

    private suspend fun base() = serverUrl().trimEnd('/')

    private suspend fun HttpResponse.successBody(): String {
        if (!status.isSuccess()) throw ReviewShareException("Review server answered ${status.value}")
        return bodyAsText()
    }

    /** Every failure, from a refused connection to a malformed answer, becomes one [ReviewShareException]. */
    private suspend fun <T> call(block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ReviewShareException) {
        throw e
    } catch (e: Exception) {
        throw ReviewShareException("Review server unreachable: ${e.message ?: e::class.simpleName}", e)
    }
}
