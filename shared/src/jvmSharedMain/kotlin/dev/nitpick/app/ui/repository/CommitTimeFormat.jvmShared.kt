package dev.nitpick.app.ui.repository

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// java.time exists on the desktop JVM and on Android from API 26 (below our minSdk), so one
// actual serves both and follows the system locale (EN or FR) for free.
actual fun formatCommitTime(epochSeconds: Long): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochSecond(epochSeconds))

actual fun formatCommitTimeCompact(epochSeconds: Long): String {
    val zone = ZoneId.systemDefault()
    val moment = Instant.ofEpochSecond(epochSeconds).atZone(zone)
    val today = LocalDate.now(zone)
    val pattern = when {
        moment.toLocalDate() == today -> "HH:mm"
        moment.year == today.year -> "d MMM"
        else -> "MMM yyyy"
    }
    return DateTimeFormatter.ofPattern(pattern).format(moment)
}
