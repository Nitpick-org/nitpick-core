package dev.nitpick.app.ui

/**
 * Small, dependency-free date formatting for commit metadata. Commit timestamps are UTC
 * epoch seconds; the UI shows them as `YYYY-MM-DD HH:MM UTC` so the graph stays readable
 * without pulling a date-time library into commonMain.
 */
fun formatCommitTimestamp(epochSeconds: Long): String {
    val days = floorDiv(epochSeconds, 86_400)
    val secondsOfDay = floorMod(epochSeconds, 86_400)
    val (year, month, day) = civilFromDays(days)
    val hour = secondsOfDay / 3_600
    val minute = (secondsOfDay % 3_600) / 60
    return buildString {
        append(year.toString().padStart(4, '0'))
        append('-')
        append(month.toString().padStart(2, '0'))
        append('-')
        append(day.toString().padStart(2, '0'))
        append(' ')
        append(hour.toString().padStart(2, '0'))
        append(':')
        append(minute.toString().padStart(2, '0'))
        append(" UTC")
    }
}

private fun floorDiv(value: Long, divisor: Long): Long =
    if (value >= 0) value / divisor else -((-value + divisor - 1) / divisor)

private fun floorMod(value: Long, divisor: Long): Long = value - floorDiv(value, divisor) * divisor

/** Howard Hinnant's civil-from-days algorithm, shifted to the Unix epoch. */
private fun civilFromDays(daysSinceEpoch: Long): Triple<Int, Int, Int> {
    val z = daysSinceEpoch + 719_468
    val era = if (z >= 0) z / 146_097 else (z - 146_096) / 146_097
    val dayOfEra = z - era * 146_097
    val yearOfEra = (dayOfEra - dayOfEra / 1_460 + dayOfEra / 36_524 - dayOfEra / 146_096) / 365
    var year = yearOfEra + era * 400
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthPart = (5 * dayOfYear + 2) / 153
    val day = dayOfYear - (153 * monthPart + 2) / 5 + 1
    val month = monthPart + if (monthPart < 10) 3 else -9
    if (month <= 2) year += 1
    return Triple(year.toInt(), month.toInt(), day.toInt())
}
