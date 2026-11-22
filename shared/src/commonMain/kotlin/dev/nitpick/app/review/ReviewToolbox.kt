package dev.nitpick.app.review

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import dev.nitpick.app.domain.ReviewToolUse
import dev.nitpick.app.git.GitRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

const val REVIEW_TOOL_LIST_FILES = "list_files"
const val REVIEW_TOOL_READ_FILE = "read_file"
const val MAX_REVIEW_TOOL_CALLS = 6
const val MAX_REVIEW_TOOL_BYTES = 64_000

data class ToolOutput(val text: String, val isError: Boolean = false)

/**
 * The two read-only tools the review agent may call. The model can inspect a neighbouring
 * file (a signature, a test) without receiving the whole repository.
 *
 * Paths come from the model: they stay relative, inside the work tree, and never touch
 * `.git`. Calls and bytes are capped per review so a loop cannot inflate the bill.
 */
class ReviewToolbox(
    private val repository: GitRepository,
    private val maxCalls: Int = MAX_REVIEW_TOOL_CALLS,
    private val maxBytes: Int = MAX_REVIEW_TOOL_BYTES,
    private val onUse: (ReviewToolUse) -> Unit = {},
) {
    private var calls = 0
    private var bytesRead = 0
    private var trackedFiles: Set<String>? = null

    fun descriptors(): List<ToolDescriptor> = listOf(
        ToolDescriptor(
            name = REVIEW_TOOL_LIST_FILES,
            description = "List the tracked files of the repository. Use it to find a related file, then read it with read_file.",
            requiredParameters = emptyList(),
            optionalParameters = emptyList(),
        ),
        ToolDescriptor(
            name = REVIEW_TOOL_READ_FILE,
            description = "Read one tracked text file from the repository. Path is relative to the repository root.",
            requiredParameters = listOf(
                ToolParameterDescriptor("path", "Relative path of the file", ToolParameterType.String),
            ),
            optionalParameters = emptyList(),
        ),
    )

    suspend fun execute(tool: String, args: JsonObject): ToolOutput {
        if (calls >= maxCalls) return ToolOutput("Tool budget exhausted ($maxCalls calls).", isError = true)
        return when (tool) {
            REVIEW_TOOL_LIST_FILES -> {
                calls++
                onUse(ReviewToolUse(REVIEW_TOOL_LIST_FILES))
                val listing = repository.listFiles()
                    .filter(::isReviewablePath)
                    .take(500)
                    .joinToString("\n")
                val listingBytes = listing.encodeToByteArray().size
                if (listingBytes > maxBytes) {
                    ToolOutput("Read budget exhausted ($maxBytes bytes).", isError = true)
                } else {
                    bytesRead += listingBytes
                    ToolOutput(listing)
                }
            }
            REVIEW_TOOL_READ_FILE -> {
                val path = (args["path"] as? JsonPrimitive)?.contentOrNull
                onUse(ReviewToolUse(REVIEW_TOOL_READ_FILE, path))
                readFile(path)
            }
            else -> ToolOutput("Unknown tool: $tool", isError = true)
        }
    }

    private suspend fun readFile(rawPath: String?): ToolOutput {
        calls++
        val path = rawPath?.trim()?.replace('\\', '/')
        if (path.isNullOrEmpty()) return ToolOutput("read_file needs a relative path.", isError = true)
        if (path.startsWith("/") || Regex("^[A-Za-z]:").containsMatchIn(path)) {
            return ToolOutput("Absolute paths are not allowed.", isError = true)
        }
        val segments = path.split('/')
        if (segments.any { it.isEmpty() || it == "." || it == ".." || it.equals(".git", ignoreCase = true) }) {
            return ToolOutput("Path must stay inside the repository and outside .git.", isError = true)
        }
        val tracked = trackedFiles ?: repository.listFiles().toSet().also { trackedFiles = it }
        if (path !in tracked) return ToolOutput("File is not tracked: $path", isError = true)
        if (!isReviewablePath(path)) return ToolOutput("Binary, generated or ignored file: $path", isError = true)
        val content = try {
            repository.readFile(path)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            // A tracked symlink that resolves outside the work tree is refused by the repository;
            // the model gets a tool error instead of losing the whole review (#157).
            return ToolOutput("File is not readable: $path", isError = true)
        } ?: return ToolOutput("File not found or not readable: $path", isError = true)
        if (content.any { it == '\u0000' }) {
            return ToolOutput("Binary file: $path", isError = true)
        }
        val contentBytes = content.encodeToByteArray().size
        if (bytesRead + contentBytes > maxBytes) {
            return ToolOutput("Read budget exhausted ($maxBytes bytes).", isError = true)
        }
        bytesRead += contentBytes
        return ToolOutput(content)
    }
}
