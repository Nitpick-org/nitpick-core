package dev.nitpick.app.review

import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.FileDiff

data class ReviewPrompt(val system: String, val user: String)

private const val LINE_NUMBER_WIDTH = 5

val REVIEW_SYSTEM_PROMPT = """
You are Nitpick, a meticulous code reviewer. You review the diff of a branch before it is pushed.
Report real problems only: bugs, security issues, data loss, broken edge cases, misleading names, dead code.
Do not praise, do not restate the diff, do not comment on formatting a linter would catch.

Answer with ONE JSON object and nothing else, no markdown fences:
{
  "summary": "one or two sentences",
  "comments": [
    {
      "filePath": "path exactly as shown in the diff",
      "startLine": 42,
      "endLine": 42,
      "severity": "BUG" | "WARNING" | "SUGGESTION" | "INFO",
      "category": "logic" | "security" | "perf" | "readability" | "tests" | "other",
      "message": "what is wrong and why, in plain words",
      "originalSnippet": "the exact text of lines startLine..endLine, copied verbatim",
      "suggestion": "replacement text for lines startLine..endLine, or null"
    }
  ]
}

Rules:
- Line numbers are the numbers shown at the left of each diff line: they are line numbers in the NEW version of the file.
- originalSnippet must be copied verbatim from the numbered lines; it is used to verify your anchor.
- Only comment on added or context lines (lines with a number). A removed line has no number.
- A comment about a whole file uses startLine = 0 and endLine = 0.
- suggestion replaces lines startLine..endLine entirely; keep the original indentation; null when there is nothing to apply.
- Prefer few precise comments over many vague ones. Severity BUG only when the code is wrong.
""".trim()

fun buildReviewPrompt(context: ReviewContext): ReviewPrompt {
    val user = buildString {
        appendLine("Branch `${context.branch}` reviewed against `${context.baseRef}`.")
        context.language?.let { appendLine("Main language: $it.") }
        if (context.recentCommits.isNotEmpty()) {
            appendLine()
            appendLine("Recent commit messages:")
            context.recentCommits.forEach { appendLine("- $it") }
        }
        context.readme?.let {
            appendLine()
            appendLine("README (truncated):")
            appendLine(it)
        }
        if (context.fileTree.isNotEmpty()) {
            appendLine()
            appendLine("Repository files (truncated):")
            context.fileTree.forEach { appendLine(it) }
        }
        appendLine()
        appendLine("## Diff")
        context.diffs.forEach { diff -> appendDiff(diff) }
        val printable = filesWithinBudget(context.files)
        if (printable.isNotEmpty()) {
            appendLine()
            appendLine("## Full content of the changed files (new version, numbered)")
            printable.forEach { (path, content) ->
                appendLine()
                appendLine("### $path")
                content.lines().forEachIndexed { index, line ->
                    appendLine("${(index + 1).toString().padStart(LINE_NUMBER_WIDTH)} | $line")
                }
            }
        }
    }
    return ReviewPrompt(system = REVIEW_SYSTEM_PROMPT, user = user)
}

/** Small models have small windows: print full files until the budget is spent, the diff always goes in. */
fun filesWithinBudget(files: Map<String, String>): Map<String, String> {
    var total = 0
    return files.filter { (_, content) ->
        val fits = content.length <= MAX_PROMPT_FILE_CHARS && total + content.length <= MAX_PROMPT_TOTAL_CHARS
        if (fits) total += content.length
        fits
    }
}

private fun StringBuilder.appendDiff(diff: FileDiff) {
    appendLine()
    appendLine("### ${diff.path} (${diff.changeType.name.lowercase()}${diff.oldPath?.let { ", was $it" } ?: ""})")
    diff.hunks.forEach { hunk ->
        appendLine("@@ -${hunk.oldStart},${hunk.oldCount} +${hunk.newStart},${hunk.newCount} @@")
        hunk.lines.forEach { line ->
            val number = line.newLineNumber?.toString()?.padStart(LINE_NUMBER_WIDTH) ?: " ".repeat(LINE_NUMBER_WIDTH)
            val marker = when (line.type) {
                DiffLineType.ADDED -> "+"
                DiffLineType.REMOVED -> "-"
                DiffLineType.CONTEXT -> " "
            }
            appendLine("$number | $marker${line.content}")
        }
    }
}
