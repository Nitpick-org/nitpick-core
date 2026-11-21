package dev.nitpick.app.ui.repository

import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.Severity

private const val MAX_SUBJECT_LENGTH = 72

/** A conventional subject naming the applied nits, e.g. `fix: unlock on cache miss, keep capacity entries`. */
internal fun commitMessageFor(applied: List<ReviewComment>): String {
    if (applied.isEmpty()) return ""
    val type = if (applied.any { it.severity == Severity.BUG }) "fix" else "refactor"
    val titles = applied
        .map { nit -> nit.title.ifBlank { nit.message.substringBefore(". ").trimEnd('.') }.trim().replaceFirstChar(Char::lowercase) }
        .distinct()
    val subject = "$type: " + titles.joinToString(", ")
    return if (subject.length <= MAX_SUBJECT_LENGTH) subject else subject.take(MAX_SUBJECT_LENGTH - 1).trimEnd() + "…"
}
