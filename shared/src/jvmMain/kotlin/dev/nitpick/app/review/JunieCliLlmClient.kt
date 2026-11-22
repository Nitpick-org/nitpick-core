package dev.nitpick.app.review

import dev.nitpick.app.settings.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.nio.file.Files

private const val POLL_MS = 200L

@Serializable
private data class JunieOutput(val result: String? = null, val errors: List<String> = emptyList())

private val junieJson = Json { ignoreUnknownKeys = true }

/**
 * Desktop only: one review = one run of the Junie CLI signed in on this machine. Junie gets an
 * empty project, never the open repository: the diff is in the prompt, and its own tools must
 * not touch the user's files.
 */
class JunieCliLlmClient(
    private val settingsStore: SettingsStore,
    private val binary: String = findJunieBinary(),
) : LlmClient {
    private val sandbox: File by lazy { Files.createTempDirectory("nitpick-junie").toFile() }

    override suspend fun complete(system: String, user: String): String = withContext(Dispatchers.IO) {
        val model = settingsStore.load().model
        val id = System.nanoTime()
        val output = File(sandbox, "answer-$id.json")
        // The prompt goes through a file Junie reads with its own tools: a command-line argument
        // is capped by the OS (128 KiB on Linux) and visible to every local process (#249).
        val taskFile = File(sandbox, "review-task-$id.md").apply { writeText("$system\n\n$user") }
        val process = try {
            ProcessBuilder(junieCommand(binary, model, sandbox, output, taskFile))
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
        } catch (e: IOException) {
            throw JunieUnavailableException("Junie CLI not found: $binary", e)
        }
        try {
            process.outputStream.close()
            // Polling keeps the call cancellable: the review timeout then kills the process below.
            while (process.isAlive) delay(POLL_MS)
            if (!output.exists()) {
                throw JunieUnavailableException("Junie exited (${process.exitValue()}) without an answer")
            }
            parseJunieOutput(output.readText())
        } finally {
            process.destroy()
            output.delete()
            taskFile.delete()
        }
    }
}

/**
 * The exact flag set validated against Junie 26.11: without `--act-as-interactive` the
 * `result` is Junie's summary of its work, not the model's answer; without the MCP opt-out it
 * loads the user's own MCP servers on every review.
 */
internal fun junieCommand(binary: String, model: String, project: File, output: File, taskFile: File): List<String> = listOf(
    binary,
    "--task", "Read the file ${taskFile.name} in this project and follow its instructions exactly. " +
        "Do not edit any file. Your whole answer must be the JSON object the file asks for, nothing else.",
    "--model", model,
    "--project", project.path,
    "--output-format", "json",
    "--json-output-file", output.path,
    "--act-as-interactive",
    "--mcp-default-locations", "false",
)

/**
 * The envelope exits 0 even on failure: `errors` decides, never the exit code. An answer wins
 * over `errors`: a failed user hook (session end, #246) is reported next to a complete review.
 */
internal fun parseJunieOutput(text: String): String {
    val output = try {
        junieJson.decodeFromString(JunieOutput.serializer(), text)
    } catch (e: SerializationException) {
        throw ReviewProviderException("Junie answered outside its JSON envelope", e)
    }
    output.result?.let { return it }
    if (output.errors.isNotEmpty()) {
        val message = output.errors.joinToString("; ")
        val signedOut = output.errors.any { "authoriz" in it.lowercase() || "authenticat" in it.lowercase() }
        if (signedOut) throw JunieUnavailableException(message)
        throw ReviewProviderException("Junie failed: $message", IllegalStateException(message))
    }
    throw ReviewProviderException("Junie answered without a result", IllegalStateException())
}

/** An app launched from the Dock has launchd's PATH, which skips the installer's `~/.local/bin`. */
internal fun findJunieBinary(): String {
    val home = System.getProperty("user.home")
    val installed = listOf(File(home, ".local/bin/junie"), File("/usr/local/bin/junie"), File("/opt/homebrew/bin/junie"))
    return installed.firstOrNull { it.canExecute() }?.path ?: "junie"
}
