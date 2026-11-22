package dev.nitpick.app.review

import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val ARG_SEPARATOR = '\u001F'

class JunieCliLlmClientTest {

    private val store = object : SettingsStore {
        override suspend fun load() = Settings(provider = LlmProvider.JUNIE, model = "junie-lite")
        override suspend fun save(settings: Settings) = Unit
    }

    /**
     * A shell script standing in for `junie`: records its arguments, copies the task file it is
     * told to read (deleted after the run) and writes [envelope] to the output file.
     */
    private fun fakeJunie(envelope: String, argsFile: File): String {
        val script = createTempFile("junie", ".sh").toFile()
        script.writeText(
            """
            #!/bin/sh
            printf '%s\037' "$@" > "${argsFile.path}"
            while [ "$1" != "--project" ]; do shift; done
            cat "$2"/review-task-*.md > "${argsFile.path}.task" 2>/dev/null
            while [ "$1" != "--json-output-file" ]; do shift; done
            printf '%s' '$envelope' > "$2"
            """.trimIndent(),
        )
        script.setExecutable(true)
        return script.path
    }

    @Test
    fun returnsTheModelAnswerAndKeepsTheValidatedFlags() = runTest {
        val argsFile = createTempFile("junie-args").toFile()
        val binary = fakeJunie("""{"result":"{\"summary\":\"ok\",\"comments\":[]}","changes":[]}""", argsFile)

        val answer = JunieCliLlmClient(store, binary).complete("system text", "user text")

        assertEquals("""{"summary":"ok","comments":[]}""", answer)
        val args = argsFile.readText().split(ARG_SEPARATOR)
        val task = args[args.indexOf("--task") + 1]
        assertTrue("review-task-" in task && "follow its instructions" in task, task)
        assertEquals("system text\n\nuser text", File("${argsFile.path}.task").readText())
        assertEquals("junie-lite", args[args.indexOf("--model") + 1])
        assertTrue("--act-as-interactive" in args, "result must be the answer, not Junie's summary")
        assertEquals("false", args[args.indexOf("--mcp-default-locations") + 1])
    }

    @Test
    fun aSignedOutCliIsReportedAsUnavailableNotAsANetworkError() = runTest {
        val binary = fakeJunie("""{"errors":["Cannot find authorization. Please authenticate before running Junie."]}""", createTempFile("junie-args").toFile())

        assertFailsWith<JunieUnavailableException> { JunieCliLlmClient(store, binary).complete("s", "u") }
    }

    @Test
    fun aMissingBinaryIsReportedAsUnavailable() = runTest {
        assertFailsWith<JunieUnavailableException> { JunieCliLlmClient(store, "/nonexistent/junie").complete("s", "u") }
    }

    @Test
    fun aCliThatWritesNoAnswerIsReportedAsUnavailable() = runTest {
        val script = createTempFile("junie", ".sh").toFile().apply { writeText("#!/bin/sh\nexit 3\n"); setExecutable(true) }

        assertFailsWith<JunieUnavailableException> { JunieCliLlmClient(store, script.path).complete("s", "u") }
    }

    @Test
    fun aLargePromptGoesThroughTheFileAndTheFileIsRemovedAfterTheRun() = runTest {
        val argsFile = createTempFile("junie-args").toFile()
        val binary = fakeJunie("""{"result":"{}"}""", argsFile)
        val big = "x".repeat(400_000)

        JunieCliLlmClient(store, binary).complete("s", big)

        assertEquals("s\n\n$big", File("${argsFile.path}.task").readText())
        val project = argsFile.readText().split(ARG_SEPARATOR).let { it[it.indexOf("--project") + 1] }
        assertTrue(File(project).listFiles().orEmpty().none { it.name.startsWith("review-task-") }, "task file left behind")
    }

    @Test
    fun anAnswerWinsOverAFailedUserHook() {
        val envelope = """{"result":"{\"summary\":\"ok\",\"comments\":[]}","errors":["Session end hook failed: python3 session-end-llma.py: No such file"]}"""

        assertEquals("""{"summary":"ok","comments":[]}""", parseJunieOutput(envelope))
    }

    @Test
    fun anyOtherJunieErrorIsAProviderFailure() {
        assertFailsWith<ReviewProviderException> { parseJunieOutput("""{"errors":["model quota exceeded"]}""") }
        assertFailsWith<ReviewProviderException> { parseJunieOutput("not json") }
        assertFailsWith<ReviewProviderException> { parseJunieOutput("""{"changes":[]}""") }
    }
}
