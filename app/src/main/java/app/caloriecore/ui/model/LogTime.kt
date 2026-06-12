package app.caloriecore.ui.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.abs

private val PhoneZone: ZoneId = ZoneId.systemDefault()
private val LogDateFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val LogClockFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun phoneNowMillis(): Long = System.currentTimeMillis()

fun newLogId(): Long {
    val millis = phoneNowMillis()
    val nanos = abs(System.nanoTime() % 1_000_000L)
    return millis * 1_000_000L + nanos
}

fun logDateText(millis: Long): String = Instant.ofEpochMilli(millis)
    .atZone(PhoneZone)
    .toLocalDate()
    .format(LogDateFormat)

fun logTimeText(millis: Long): String = Instant.ofEpochMilli(millis)
    .atZone(PhoneZone)
    .toLocalTime()
    .format(LogClockFormat)

fun logMomentText(millis: Long): String = "${logDateText(millis)} ${logTimeText(millis)}"

fun parseLogMoment(date: String, time: String): Long? = try {
    val parsedDate = LocalDate.parse(date.trim(), LogDateFormat)
    val parsedTime = LocalTime.parse(time.trim(), LogClockFormat)
    LocalDateTime.of(parsedDate, parsedTime).atZone(PhoneZone).toInstant().toEpochMilli()
} catch (_: DateTimeParseException) {
    null
} catch (_: IllegalArgumentException) {
    null
}

fun samePhoneDay(firstMillis: Long, secondMillis: Long): Boolean =
    logDateText(firstMillis) == logDateText(secondMillis)

fun phoneDayStartMillis(anchorMillis: Long): Long = Instant.ofEpochMilli(anchorMillis)
    .atZone(PhoneZone)
    .toLocalDate()
    .atStartOfDay(PhoneZone)
    .toInstant()
    .toEpochMilli()

fun phoneDayEndMillis(anchorMillis: Long): Long = Instant.ofEpochMilli(anchorMillis)
    .atZone(PhoneZone)
    .toLocalDate()
    .plusDays(1)
    .atStartOfDay(PhoneZone)
    .toInstant()
    .toEpochMilli() - 1L
