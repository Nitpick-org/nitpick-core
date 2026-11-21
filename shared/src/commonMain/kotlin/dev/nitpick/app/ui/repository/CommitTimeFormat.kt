package dev.nitpick.app.ui.repository

/** Commit date and time in the user's locale and time zone. */
expect fun formatCommitTime(epochSeconds: Long): String
