package dev.nitpick.app.review

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import dev.nitpick.app.git.GitRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

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
) {
    private var calls = 0
    private var bytesRead = 0

    fun descriptors(): List<ToolDescriptor> = listOf(
        ToolDescriptor(
            name = "list_files",
            description = "List the tracked files of the repository. Use it to find a related file, then read it with read_file.",
            requiredParameters = emptyList(),
            optionalParameters = emptyList(),
        ),
        ToolDescriptor(
            name = "read_file",
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
            "list_files" -> {
                calls++
                ToolOutput(repository.listFiles().take(500).joinToString("\n"))
            }
            "read_file" -> readFile(args["path"]?.jsonPrimitive?.contentOrNull)
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
        if (segments.any { it.isEmpty() || it == "." || it == ".." || it == ".git" }) {
            return ToolOutput("Path must stay inside the repository and outside .git.", isError = true)
        }
        val content = repository.readFile(path)
            ?: return ToolOutput("File not found or not readable: $path", isError = true)
        val contentBytes = content.encodeToByteArray().size
        if (bytesRead + contentBytes > maxBytes) {
            return ToolOutput("Read budget exhausted ($maxBytes bytes).", isError = true)
        }
        bytesRead += contentBytes
        return ToolOutput(content)
    }
}
