package dev.nitpick.app.ui.repository

/** Commit date and time in the user's locale and time zone. */
expect fun formatCommitTime(epochSeconds: Long): String

/** The same instant for a narrow column: time today, day and month this year, month and year before. */
expect fun formatCommitTimeCompact(epochSeconds: Long): String
