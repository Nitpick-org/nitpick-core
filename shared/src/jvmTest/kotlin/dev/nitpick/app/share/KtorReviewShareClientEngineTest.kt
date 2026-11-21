package dev.nitpick.app.share

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith

class KtorReviewShareClientEngineTest {

    // HttpClient() must find the CIO engine of jvmSharedMain; nothing listens on port 1.
    @Test
    fun aStoppedServerIsACleanErrorWithTheRealEngine() {
        val client = KtorReviewShareClient({ "http://127.0.0.1:1" })

        assertFailsWith<ReviewShareException> { runBlocking { client.list("https://github.com/Nitpick-org/pocket-ledger") } }
    }
}
