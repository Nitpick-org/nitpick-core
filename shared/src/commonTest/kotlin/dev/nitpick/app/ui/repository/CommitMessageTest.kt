package dev.nitpick.app.ui.repository

import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommitMessageTest {

    private fun nit(id: String, severity: Severity, message: String, title: String = "") = ReviewComment(
        id = id, filePath = "a.kt", startLine = 1, endLine = 1, severity = severity, category = "logic",
        message = message, originalSnippet = "x", suggestion = "y", title = title,
    )

    @Test
    fun namesTheAppliedFixesFromTheirTitles() {
        val applied = listOf(
            nit("1", Severity.BUG, "The lock stays held.", title = "Unlock on cache miss"),
            nit("2", Severity.WARNING, "Off by one.", title = "Keep capacity entries"),
        )
        assertEquals("fix: unlock on cache miss, keep capacity entries", commitMessageFor(applied))
    }

    @Test
    fun fallsBackToTheFirstSentenceAndRefactorWithoutABug() {
        val applied = listOf(nit("1", Severity.SUGGESTION, "Name says lane but holds a column. Rename it."))
        assertEquals("refactor: name says lane but holds a column", commitMessageFor(applied))
        assertEquals("", commitMessageFor(emptyList()))
    }

    @Test
    fun staysWithinOneSubjectLine() {
        val applied = (1..10).map { nit("$it", Severity.BUG, "m", title = "a rather long title number $it") }
        val subject = commitMessageFor(applied)
        assertTrue(subject.length <= 72, subject)
        assertTrue(subject.endsWith("…"))
    }
}
