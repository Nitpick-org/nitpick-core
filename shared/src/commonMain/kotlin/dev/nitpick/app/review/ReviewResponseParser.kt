package dev.nitpick.app.review

import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.domain.Severity
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ReviewResponseException(message: String) : Exception(message)

@Serializable
private data class ReviewResponse(
    val summary: String = "",
    val comments: List<CommentResponse> = emptyList(),
)

@Serializable
private data class CommentResponse(
    val filePath: String,
    val startLine: Int = 0,
    val endLine: Int? = null,
    val severity: String = "INFO",
    val category: String = "other",
    val message: String,
    val originalSnippet: String = "",
    val suggestion: String? = null,
    val title: String = "",
)

private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

/** Models wrap JSON in prose or fences more often than not: keep the outermost object only. */
fun parseReviewResponse(text: String): ReviewResult {
    val start = text.indexOf('{')
    val end = text.lastIndexOf('}')
    if (start < 0 || end <= start) throw ReviewResponseException("The model answer contains no JSON object")
    val response = try {
        lenientJson.decodeFromString(ReviewResponse.serializer(), text.substring(start, end + 1))
    } catch (e: SerializationException) {
        throw ReviewResponseException("The model answer is not a valid review: ${e.message}")
    } catch (e: IllegalArgumentException) {
        throw ReviewResponseException("The model answer is not a valid review: ${e.message}")
    }
    return ReviewResult(
        summary = response.summary,
        comments = response.comments.mapIndexed { index, comment ->
            ReviewComment(
                id = "${comment.filePath}:${comment.startLine}:$index",
                filePath = comment.filePath,
                startLine = comment.startLine.coerceAtLeast(0),
                endLine = (comment.endLine ?: comment.startLine).coerceAtLeast(comment.startLine),
                severity = Severity.entries.firstOrNull { it.name.equals(comment.severity.trim(), ignoreCase = true) }
                    ?: Severity.INFO,
                category = comment.category,
                message = comment.message,
                originalSnippet = comment.originalSnippet,
                // Apply finds the lines by their snippet: without one there is nothing to replace.
                suggestion = comment.suggestion?.takeIf { it.isNotBlank() && comment.originalSnippet.isNotBlank() },
                title = comment.title.trim(),
            )
        },
    )
}
