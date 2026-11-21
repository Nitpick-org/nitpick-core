package dev.nitpick.app.settings

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.ParametersBuilder
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

/**
 * OAuth client ids are public (the device flow needs no secret). Create an OAuth App on the forge
 * with the device flow enabled and paste its id here; an empty id hides the sign-in button.
 */
object ForgeOAuthClientIds {
    const val GITHUB = "Ov23liWfSIx46RdkA69k"
    const val GITLAB = ""

    fun forHost(host: GitHost): String = when (host) {
        GitHost.GITHUB -> GITHUB
        GitHost.GITLAB -> GITLAB
    }
}

/** What the forge hands back when a sign-in starts: the code the user types, and where. */
data class DeviceCode(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresInSeconds: Int,
    val intervalSeconds: Int,
)

class DeviceFlowException(message: String, cause: Throwable? = null) : Exception(message, cause)

private class ForgeEndpoints(val deviceUrl: String, val tokenUrl: String, val scope: String)

// Scopes: GitHub needs `repo` for private repositories; GitLab lets git over HTTPS through with
// read_repository (clone) and write_repository (push).
private fun GitHost.endpoints() = when (this) {
    GitHost.GITHUB -> ForgeEndpoints(
        deviceUrl = "https://github.com/login/device/code",
        tokenUrl = "https://github.com/login/oauth/access_token",
        scope = "repo",
    )
    // shortcut: a GitLab device-flow token lives 2 h and comes without a refresh token, so the user
    // signs in again afterwards; add the refresh grant when that becomes a complaint.
    GitHost.GITLAB -> ForgeEndpoints(
        deviceUrl = "https://gitlab.com/oauth/authorize_device",
        tokenUrl = "https://gitlab.com/oauth/token",
        scope = "read_repository write_repository",
    )
}

private const val DEVICE_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code"
private const val SLOW_DOWN_EXTRA_SECONDS = 5
private const val AUTHORIZATION_PENDING = "authorization_pending"
private const val SLOW_DOWN = "slow_down"

@Serializable
private data class DeviceCodeResponse(
    @SerialName("device_code") val deviceCode: String,
    @SerialName("user_code") val userCode: String,
    @SerialName("verification_uri") val verificationUri: String,
    // GitLab also sends the URL with the code filled in; GitHub does not.
    @SerialName("verification_uri_complete") val verificationUriComplete: String? = null,
    @SerialName("expires_in") val expiresIn: Int,
    val interval: Int = 5,
)

@Serializable
private data class TokenResponse(
    @SerialName("access_token") val accessToken: String? = null,
    val error: String? = null,
    val interval: Int? = null,
)

private val json = Json { ignoreUnknownKeys = true }

/**
 * OAuth 2.0 device flow (RFC 8628) against github.com and gitlab.com. GitHub answers a pending poll
 * with HTTP 200 and an `error` field, GitLab with HTTP 400: the body is read whatever the status.
 * `HttpClient()` picks the CIO engine from jvmSharedMain; tests pass a MockEngine client.
 */
class DeviceFlowClient(
    private val http: HttpClient = HttpClient {
        // A forge that hangs must fail within seconds, not leave the code screen waiting forever.
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 15_000
        }
    },
) {

    suspend fun start(host: GitHost, clientId: String): DeviceCode = call {
        val endpoints = host.endpoints()
        val response = post(endpoints.deviceUrl) {
            append("client_id", clientId)
            append("scope", endpoints.scope)
        }
        val body = response.decode<DeviceCodeResponse>()
        DeviceCode(
            deviceCode = body.deviceCode,
            userCode = body.userCode,
            verificationUri = body.verificationUriComplete ?: body.verificationUri,
            expiresInSeconds = body.expiresIn,
            intervalSeconds = body.interval,
        )
    }

    /** Polls until the user approves in the browser; the token is returned once and never logged. */
    suspend fun awaitToken(host: GitHost, clientId: String, code: DeviceCode): String = call {
        poll(host.endpoints().tokenUrl, clientId, code)
    }

    // Counts the waits instead of a timeout, so tests on virtual time stay deterministic.
    private suspend fun poll(tokenUrl: String, clientId: String, code: DeviceCode): String {
        // Never 0: a forge answering interval 0 would be polled without pause, forever.
        var interval = code.intervalSeconds.coerceAtLeast(1)
        var waited = 0
        while (true) {
            delay(interval.seconds)
            waited += interval
            if (waited > code.expiresInSeconds) throw DeviceFlowException("expired_token")
            val answer = try {
                post(tokenUrl) {
                    append("client_id", clientId)
                    append("device_code", code.deviceCode)
                    append("grant_type", DEVICE_GRANT_TYPE)
                }.decode<TokenResponse>()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // A phone's network blips; the code is still valid, so ask again at the next tick.
                continue
            }
            when (answer.error) {
                null -> return answer.accessToken ?: throw DeviceFlowException("no token in the answer")
                AUTHORIZATION_PENDING -> Unit
                SLOW_DOWN -> interval = (answer.interval ?: (interval + SLOW_DOWN_EXTRA_SECONDS)).coerceAtLeast(1)
                else -> throw DeviceFlowException(answer.error)
            }
        }
    }

    private suspend fun post(url: String, form: ParametersBuilder.() -> Unit): HttpResponse =
        http.submitForm(url, formParameters = parameters(form)) {
            // Without it GitHub answers form-encoded.
            header(HttpHeaders.Accept, "application/json")
        }

    // The body is never put in an exception: on success it holds the token.
    private suspend inline fun <reified T> HttpResponse.decode(): T = try {
        json.decodeFromString<T>(bodyAsText())
    } catch (e: SerializationException) {
        if (status.isSuccess()) throw DeviceFlowException("unexpected answer from the forge", e)
        throw DeviceFlowException("the forge answered ${status.value}", e)
    } catch (e: IllegalArgumentException) {
        throw DeviceFlowException("unexpected answer from the forge", e)
    }

    private suspend fun <T> call(block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: DeviceFlowException) {
        throw e
    } catch (e: Exception) {
        throw DeviceFlowException("forge unreachable: ${e::class.simpleName}", e)
    }
}
