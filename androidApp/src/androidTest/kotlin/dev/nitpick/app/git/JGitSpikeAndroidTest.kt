package dev.nitpick.app.git

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

// Runs on a real device or emulator: host tests would use the desktop JVM, not ART.
class JGitSpikeAndroidTest {

    @Test
    fun jgitCommitsAndReadsTheLogOnArt() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(context.cacheDir, "nitpick-spike")
        dir.deleteRecursively()
        try {
            assertEquals(listOf("first commit"), jgitSmokeLog(dir))
        } finally {
            dir.deleteRecursively()
        }
    }
}
