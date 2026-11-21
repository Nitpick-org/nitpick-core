package dev.nitpick.app.git

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class JGitSpikeDesktopTest {

    @Test
    fun jgitCommitsAndReadsTheLog() {
        val dir = Files.createTempDirectory("nitpick-spike").toFile()
        try {
            assertEquals(listOf("first commit"), jgitSmokeLog(dir))
        } finally {
            dir.deleteRecursively()
        }
    }
}
