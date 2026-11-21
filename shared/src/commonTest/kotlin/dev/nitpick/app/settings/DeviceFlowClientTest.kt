package dev.nitpick.app.settings

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.parseUrlEncodedParameters
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeviceFlowClientTest {

    private val requests = mutableListOf<HttpRequestData>()
    private val code = DeviceCode("dev-code", "WDJB-MJHT", "https://github.com/login/device", expiresInSeconds = 900, intervalSeconds = 5)

    private fun client(vararg answers: Pair<String, HttpStatusCode>): DeviceFlowClient {
        val queue = answers.toMutableList()
        return DeviceFlowClient(HttpClient(MockEngine { request -> requests += request; json(queue.removeFirst()) }))
    }

    private fun MockRequestHandleScope.json(answer: Pair<String, HttpStatusCode>): HttpResponseData =
        respond(answer.first, answer.second, headersOf(HttpHeaders.ContentType, "application/json"))

    private suspend fun HttpRequestData.form(): Map<String, String> =
        body.toByteArray().decodeToString().replace('+', ' ').parseUrlEncodedParameters().entries().associate { it.key to it.value.single() }

    @Test
    fun startAsksGitHubForACodeWithTheRepoScope() = runTest {
        val client = client("""{"device_code":"dev-code","user_code":"WDJB-MJHT","verification_uri":"https://github.com/login/device","expires_in":900,"interval":5}""" to HttpStatusCode.OK)

        val started = client.start(GitHost.GITHUB, "client-1")

        assertEquals(code, started)
        val request = requests.single()
        assertEquals("https://github.com/login/device/code", request.url.toString())
        assertEquals("application/json", request.headers[HttpHeaders.Accept])
        assertEquals(mapOf("client_id" to "client-1", "scope" to "repo"), request.form())
    }

    @Test
    fun startPrefersGitLabsPrefilledUrl() = runTest {
        val client = client("""{"device_code":"d","user_code":"0A44L90H","verification_uri":"https://gitlab.com/oauth/device","verification_uri_complete":"https://gitlab.com/oauth/device?user_code=0A44L90H","expires_in":300,"interval":5}""" to HttpStatusCode.OK)

        val started = client.start(GitHost.GITLAB, "app-id")

        assertEquals("https://gitlab.com/oauth/device?user_code=0A44L90H", started.verificationUri)
        assertEquals("read_repository write_repository", requests.single().form()["scope"])
    }

    @Test
    fun awaitTokenKeepsPollingThroughPendingAndSlowDown() = runTest {
        val client = client(
            """{"error":"authorization_pending"}""" to HttpStatusCode.OK,
            """{"error":"slow_down","interval":10}""" to HttpStatusCode.OK,
            """{"error":"authorization_pending"}""" to HttpStatusCode.BadRequest,
            """{"access_token":"gho_secret","token_type":"bearer","scope":"repo"}""" to HttpStatusCode.OK,
        )

        val token = client.awaitToken(GitHost.GITHUB, "client-1", code)

        assertEquals("gho_secret", token)
        assertEquals(4, requests.size)
        assertEquals(
            mapOf("client_id" to "client-1", "device_code" to "dev-code", "grant_type" to "urn:ietf:params:oauth:grant-type:device_code"),
            requests.last().form(),
        )
    }

    @Test
    fun awaitTokenStopsWhenTheUserRefuses() = runTest {
        val client = client("""{"error":"access_denied","error_description":"The user denied"}""" to HttpStatusCode.BadRequest)

        val failure = assertFailsWith<DeviceFlowException> { client.awaitToken(GitHost.GITLAB, "app-id", code) }

        assertEquals("access_denied", failure.message)
    }

    @Test
    fun awaitTokenGivesUpWhenTheCodeExpires() = runTest {
        val pending = """{"error":"authorization_pending"}""" to HttpStatusCode.OK
        val client = client(*Array(200) { pending })

        val failure = assertFailsWith<DeviceFlowException> {
            client.awaitToken(GitHost.GITHUB, "client-1", code.copy(expiresInSeconds = 12, intervalSeconds = 5))
        }

        assertEquals("expired_token", failure.message)
        assertEquals(2, requests.size)
    }

    @Test
    fun aZeroIntervalStillWaitsAndExpires() = runTest {
        val pending = """{"error":"authorization_pending"}""" to HttpStatusCode.OK
        val client = client(*Array(50) { pending })

        assertFailsWith<DeviceFlowException> {
            client.awaitToken(GitHost.GITHUB, "client-1", code.copy(expiresInSeconds = 3, intervalSeconds = 0))
        }

        assertEquals(3, requests.size)
    }

    @Test
    fun aServerErrorNeverLeaksTheBody() = runTest {
        val client = client("<html>Bad gateway</html>" to HttpStatusCode.BadGateway)

        val failure = assertFailsWith<DeviceFlowException> { client.start(GitHost.GITHUB, "client-1") }

        assertEquals("the forge answered 502", failure.message)
    }
}
