package dev.nitpick.app

import androidx.test.platform.app.InstrumentationRegistry
import dev.nitpick.app.git.JGitRepositoryFactory
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.zip.ZipInputStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Proves JGit opens and clones repositories on ART, not only in the dexer. The fixture is a
 * zipped `.git` folder with two commits, because the device has no git CLI.
 */
class JGitOnDeviceTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val root = File(instrumentation.targetContext.cacheDir, "jgit-test-${System.nanoTime()}")
    private val factory = JGitRepositoryFactory()

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    @Test
    fun opensARepositoryAndReadsItsLog() = runBlocking {
        val repository = factory.open(unzipFixture("origin").path)

        assertEquals(listOf("second", "first"), repository.log().map { it.shortMessage })
        assertEquals("main", repository.currentBranch())
    }

    @Test
    fun clonesALocalRepositoryAndReadsItsLog() = runBlocking {
        val origin = unzipFixture("origin")

        val clone = factory.clone(origin.toURI().toString(), File(root, "clone").path, credentials = null)

        assertEquals(listOf("second", "first"), clone.log().map { it.shortMessage })
        assertEquals("2\n", clone.readFile("b.txt"))
    }

    private fun unzipFixture(name: String): File {
        val target = File(root, name).apply { mkdirs() }
        ZipInputStream(instrumentation.context.assets.open("tiny-repo.zip")).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entry ->
                val file = File(target, entry.name)
                check(file.canonicalPath.startsWith(target.canonicalPath)) { "Zip entry escapes the folder: ${entry.name}" }
                if (entry.isDirectory) file.mkdirs() else file.apply { parentFile.mkdirs() }.outputStream().use(zip::copyTo)
            }
        }
        return target
    }
}
