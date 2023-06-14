package util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


fun get99DaysAgoFormatDate(): String = getFormatDateAgo(99L)

fun getTodayFormatDate(): String = getFormatDateAgo(0L)

fun getFormatDateAgo(count: Long): String {
    val today: LocalDate = LocalDate.now(ZoneId.of("America/New_York"))
    val before100Days: LocalDate = today.minusDays(count)
    return before100Days.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

fun parseDateFromUnixTime(unixTimestamp: Long): String {
    val instant = Instant.ofEpochSecond(unixTimestamp)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return dateTime.toString()
}