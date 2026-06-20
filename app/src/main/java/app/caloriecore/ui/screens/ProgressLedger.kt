package app.caloriecore.ui.screens

import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.burnEstimateForDay
import app.caloriecore.ui.model.gymLogsOnPhoneDay
import app.caloriecore.ui.model.mealsOnPhoneDay
import app.caloriecore.ui.model.nearestBodyCheckIn
import app.caloriecore.ui.model.sumPlateMacros
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val SmallTickDate: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd")

internal data class WeekLedgerPoint(
    val millis: Long,
    val label: String,
    val weightKg: Double,
    val intakeKcal: Int,
    val burnKcal: Int,
    val balanceKcal: Int,
    val trainingVolumeKg: Double
)

internal fun weekAroundPickedDay(logbook: Logbook, days: Int): List<WeekLedgerPoint> {
    val zone = ZoneId.systemDefault()
    val anchorDate = Instant.ofEpochMilli(logbook.selectedDateTime).atZone(zone).toLocalDate()
    return (days - 1 downTo 0).map { offset ->
        val date = anchorDate.minusDays(offset.toLong())
        // Use noon so DST does not move the day.
        val millis = date.noonBucketMillis(zone)
        val foodEntries = mealsOnPhoneDay(logbook.foodEntries, millis)
        val sessions = gymLogsOnPhoneDay(logbook.trainingSessions, millis)
        val profile = nearestBodyCheckIn(logbook.bodyHistory, millis)
        val nutrition = sumPlateMacros(foodEntries)
        val burn = burnEstimateForDay(profile, foodEntries)
        WeekLedgerPoint(
            millis = millis,
            label = date.format(SmallTickDate),
            weightKg = profile.weightKg,
            intakeKcal = nutrition.calories,
            burnKcal = burn.total,
            balanceKcal = nutrition.calories - burn.total,
            trainingVolumeKg = sessions.sumOf { it.totalVolumeKg }
        )
    }
}

private fun LocalDate.noonBucketMillis(zone: ZoneId): Long = atTime(LocalTime.NOON)
    .atZone(zone)
    .toInstant()
    .toEpochMilli()
