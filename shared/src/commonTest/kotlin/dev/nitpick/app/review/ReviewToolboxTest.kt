package dev.nitpick.app.review

import dev.nitpick.app.git.FakeGitRepository
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewToolboxTest {

    private val repository = FakeGitRepository(
        files = mapOf(
            "src/Hero.kt" to "class Hero(val name: String)",
            "src/HeroTest.kt" to "class HeroTest",
            ".git/config" to "token = secret",
            ".env" to "API_KEY=secret",
            "assets/logo.png" to "binary",
            "secret.pem" to "PRIVATE KEY",
            "local.properties" to "sdk.dir=/secret",
            "bin.dat" to "text\u0000binary",
            "big.txt" to "x".repeat(100),
        ),
    )

    private fun path(path: String): JsonObject = buildJsonObject { put("path", path) }

    @Test
    fun absoluteAndTraversalPathsAreRefused() = runTest {
        val toolbox = ReviewToolbox(repository)

        assertTrue(toolbox.execute("read_file", path("/etc/passwd")).isError)
        assertTrue(toolbox.execute("read_file", path("../secret")).isError)
        assertTrue(toolbox.execute("read_file", path(".git/config")).isError)
        assertTrue(toolbox.execute("read_file", path(".GIT/config")).isError)
        assertTrue(toolbox.execute("read_file", path(".env")).isError)
        assertTrue(toolbox.execute("read_file", path("assets/logo.png")).isError)
        assertTrue(toolbox.execute("read_file", path("secret.pem")).isError)
        assertTrue(toolbox.execute("read_file", path("local.properties")).isError)
        assertTrue(toolbox.execute("read_file", path("bin.dat")).isError)
    }

    @Test
    fun aRelativeFileIsReturnedVerbatim() = runTest {
        val toolbox = ReviewToolbox(repository)

        val output = toolbox.execute("read_file", path("src/Hero.kt"))

        assertEquals("class Hero(val name: String)", output.text)
    }

    @Test
    fun listFilesReturnsTheTrackedFiles() = runTest {
        val toolbox = ReviewToolbox(repository)

        val output = toolbox.execute("list_files", JsonObject(emptyMap()))

        assertTrue("src/Hero.kt" in output.text)
        assertTrue("src/HeroTest.kt" in output.text)
        assertTrue(".env" !in output.text)
        assertTrue("logo.png" !in output.text)
    }

    @Test
    fun theCallBudgetIsEnforced() = runTest {
        val toolbox = ReviewToolbox(repository, maxCalls = 1)

        assertFalse(toolbox.execute("list_files", JsonObject(emptyMap())).isError)
        assertTrue(toolbox.execute("list_files", JsonObject(emptyMap())).isError)
    }

    @Test
    fun theReadBudgetIsEnforced() = runTest {
        val toolbox = ReviewToolbox(repository, maxBytes = 20)

        val output = toolbox.execute("read_file", path("big.txt"))

        assertTrue(output.isError)
        assertTrue("budget" in output.text)
    }

    @Test
    fun theDescriptorsNameBothReadOnlyTools() {
        val names = ReviewToolbox(repository).descriptors().map { it.name }

        assertEquals(listOf("list_files", "read_file"), names)
    }
}
