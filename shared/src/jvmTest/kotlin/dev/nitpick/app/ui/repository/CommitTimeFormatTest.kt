package dev.nitpick.app.ui.repository

import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertTrue

class CommitTimeFormatTest {

    @Test
    fun todayShowsATimeOlderCommitsADate() {
        val now = Instant.now()
        val today = formatCommitTimeCompact(now.epochSecond)
        assertTrue(Regex("""\d{1,2}:\d{2}.*""").matches(today), today)

        val lastYear = formatCommitTimeCompact(now.minus(400, ChronoUnit.DAYS).epochSecond)
        assertTrue(Regex("""\S+ \d{4}""").matches(lastYear), lastYear)
        assertTrue(":" !in lastYear, lastYear)
    }
}
