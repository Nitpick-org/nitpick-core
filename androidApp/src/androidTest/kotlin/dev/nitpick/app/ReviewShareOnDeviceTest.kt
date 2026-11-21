package dev.nitpick.app

import dev.nitpick.app.share.KtorReviewShareClient
import dev.nitpick.app.share.ReviewShareException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith

/** Proves the review-sharing client runs on ART: `HttpClient()` finds an engine, and a stopped server is a clean error. */
class ReviewShareOnDeviceTest {

    @Test
    fun aStoppedServerIsACleanError() {
        val client = KtorReviewShareClient({ "http://127.0.0.1:1" })

        assertFailsWith<ReviewShareException> { runBlocking { client.list("https://github.com/o/r") } }
    }
}
