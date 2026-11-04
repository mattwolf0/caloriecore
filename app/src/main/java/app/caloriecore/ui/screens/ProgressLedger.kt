package app.caloriecore.ui.screens

import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.activeCaloriesTotal
import app.caloriecore.ui.model.burnEstimateForDay
import app.caloriecore.ui.model.nearestBodyCheckIn
import app.caloriecore.ui.model.phoneDayStartMillis
import app.caloriecore.ui.model.sumPlateMacros
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val SmallTickDate: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd")

internal data class ProgressDay(
    val millis: Long,
    val label: String,
    val weightKg: Double?,
    val intakeKcal: Int?,
    val burnKcal: Int?,
    val balanceKcal: Int?,
    val trainingVolumeKg: Double?,
    val steps: Int?,
    val proteinGrams: Double? = null,
    val carbGrams: Double? = null,
    val fatGrams: Double? = null,
    val bodyFat: Double? = null,
    val sleepHours: Double? = null,
    val activityKcal: Int? = null,
    val workoutCount: Int = 0,
    val setCount: Int = 0
)

internal fun daysForProgress(logbook: Logbook, dayCount: Int): List<ProgressDay> {
    val zone = ZoneId.systemDefault()
    val anchorDate = Instant.ofEpochMilli(logbook.selectedDateTime).atZone(zone).toLocalDate()
    val foodByDay = logbook.foodEntries.groupBy { phoneDayStartMillis(it.loggedAt) }
    val sessionsByDay = logbook.trainingSessions.groupBy { phoneDayStartMillis(it.loggedAt) }
    val activitiesByDay = logbook.activityEntries.groupBy { phoneDayStartMillis(it.loggedAt) }
    val bodyByDay = logbook.bodyHistory.groupBy { phoneDayStartMillis(it.loggedAt) }

    return (dayCount - 1 downTo 0).map { offset ->
        val date = anchorDate.minusDays(offset.toLong())
        // Use noon so DST does not move the day.
        val millis = date.noonBucketMillis(zone)
        val dayStart = phoneDayStartMillis(millis)
        val foodEntries = foodByDay[dayStart].orEmpty()
        val sessions = sessionsByDay[dayStart].orEmpty()
        val activities = activitiesByDay[dayStart].orEmpty()
        val profile = nearestBodyCheckIn(logbook.bodyHistory, millis)
        val nutrition = sumPlateMacros(foodEntries)
        val burn = burnEstimateForDay(profile, foodEntries, activities)
        val dayProfile = bodyByDay[dayStart]
            .orEmpty()
            .maxByOrNull { it.loggedAt }
        ProgressDay(
            millis = millis,
            label = date.format(SmallTickDate),
            weightKg = dayProfile?.weightKg,
            intakeKcal = nutrition.calories.takeIf { foodEntries.isNotEmpty() },
            burnKcal = burn.total.takeIf {
                dayProfile != null || foodEntries.isNotEmpty() || activities.isNotEmpty()
            },
            balanceKcal = (nutrition.calories - burn.total).takeIf { foodEntries.isNotEmpty() },
            trainingVolumeKg = sessions.sumOf { it.totalVolumeKg }.takeIf { sessions.isNotEmpty() },
            steps = dayProfile?.steps,
            proteinGrams = nutrition.protein.takeIf { foodEntries.isNotEmpty() && foodEntries.all { it.proteinKnown } },
            carbGrams = nutrition.carbs.takeIf { foodEntries.isNotEmpty() && foodEntries.all { it.carbsKnown } },
            fatGrams = nutrition.fat.takeIf { foodEntries.isNotEmpty() && foodEntries.all { it.fatKnown } },
            bodyFat = dayProfile?.bodyFatPercent,
            sleepHours = dayProfile?.sleepHours,
            activityKcal = activities.activeCaloriesTotal().takeIf { activities.isNotEmpty() },
            workoutCount = sessions.size,
            setCount = sessions.sumOf { it.totalSets }
        )
    }
}

private fun LocalDate.noonBucketMillis(zone: ZoneId): Long = atTime(LocalTime.NOON)
    .atZone(zone)
    .toInstant()
    .toEpochMilli()
