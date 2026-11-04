package app.caloriecore.ui.screens

import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.ActivitySource
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.LoggedExercise
import app.caloriecore.ui.model.LoggedSet
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.burnEstimateForDay
import app.caloriecore.ui.model.parseLogMoment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressLedgerTest {
    @Test
    fun macroHistoryKeepsUnknownValuesSeparateFromZero() {
        val date = parseLogMoment("2026-06-14", "18:00")!!
        val known = FoodEntry(loggedAt = date, name = "Water", servingGrams = 200, calories = 0,
            proteinGrams = 0.0, carbGrams = 0.0, fatGrams = 0.0)
        val unknown = known.copy(name = "Lunch", calories = 300, proteinGrams = 0.0,
            carbGrams = 30.0, fatGrams = 10.0, proteinKnown = false)
        val logbook = Logbook(selectedDateTime = date, foodEntries = listOf(known, unknown))
        val day = daysForProgress(logbook, 7).last()

        assertEquals(null, day.proteinGrams)
        assertEquals(30.0, day.carbGrams!!, 0.0)
        assertEquals(10.0, day.fatGrams!!, 0.0)
        val waterDay = daysForProgress(logbook.copy(foodEntries = listOf(known)), 7).last()
        assertEquals(0.0, waterDay.proteinGrams!!, 0.0)
        assertEquals(null, daysForProgress(logbook, 7).first().proteinGrams)
    }

    @Test
    fun bodyChartsUseLatestSavedValuesWithoutFillingOtherDays() {
        val date = parseLogMoment("2026-06-14", "18:00")!!
        val body = BodySnapshot(loggedAt = date, bodyFatPercent = 21.5, sleepHours = 8.0)
        val logbook = Logbook(selectedDateTime = date, profile = body,
            bodyHistory = listOf(body.copy(loggedAt = date - 3600000, bodyFatPercent = 22.0), body))
        val progressDays = daysForProgress(logbook, 7)

        assertEquals(21.5, progressDays.last().bodyFat!!, 0.0)
        assertEquals(8.0, progressDays.last().sleepHours!!, 0.0)
        assertEquals(null, progressDays.first().bodyFat)
        assertEquals(null, progressDays.first().sleepHours)
    }

    @Test
    fun activityChartOnlyCountsActivityEntriesInThePeriod() {
        val date = parseLogMoment("2026-06-14", "18:00")!!
        val body = BodySnapshot(loggedAt = date, watchActiveCalories = 400, plannedWorkoutCalories = 200)
        val activity = ActivityEntry(loggedAt = date, name = "Walk", source = ActivitySource.Manual, activeCalories = 120)
        val logbook = Logbook(selectedDateTime = date, profile = body, activityEntries = listOf(
            activity, activity.copy(id = 2, activeCalories = 80),
            activity.copy(id = 3, loggedAt = parseLogMoment("2026-05-01", "18:00")!!)
        ))
        val progressDays = daysForProgress(logbook, 7)
        assertEquals(200, progressDays.last().activityKcal)
        assertEquals(null, progressDays.first().activityKcal)
        assertEquals(200, ProgressStats(progressDays).activityCalories)
    }

    @Test
    fun showsBurnButNotBalanceOnDaysWithoutFood() {
        val date = parseLogMoment("2026-06-14", "18:00")!!
        val body = BodySnapshot(loggedAt = date)
        val activity = ActivityEntry(
            loggedAt = date, name = "Walk", source = ActivitySource.Manual, activeCalories = 220
        )
        val logbook = Logbook(selectedDateTime = date, profile = body, activityEntries = listOf(activity))
        val day = daysForProgress(logbook, 7).last()

        assertEquals(burnEstimateForDay(body, emptyList()).total + 220, day.burnKcal)
        assertEquals(null, day.balanceKcal)
        assertEquals(null, day.intakeKcal)
        assertEquals(null, daysForProgress(logbook, 7).first().burnKcal)
    }

    @Test
    fun includesLoggedActivityInDailyBalance() {
        val date = parseLogMoment("2026-06-14", "18:00")!!
        val body = BodySnapshot(loggedAt = date, plannedWorkoutCalories = 180, steps = 4100)
        val food = FoodEntry(
            loggedAt = date, name = "Lunch", servingGrams = 400, calories = 650,
            proteinGrams = 30.0, carbGrams = 65.0, fatGrams = 20.0
        )
        val activity = ActivityEntry(loggedAt = date, name = "Walk", source = ActivitySource.Manual, activeCalories = 220)
        val logbook = Logbook(selectedDateTime = date, profile = body,
            foodEntries = listOf(food), activityEntries = listOf(activity))

        val day = daysForProgress(logbook, 7).last()
        assertEquals(burnEstimateForDay(body, listOf(food)).total + 220, day.burnKcal)
        assertEquals(650 - day.burnKcal!!, day.balanceKcal)
        assertEquals(4100, day.steps)
        assertEquals(null, daysForProgress(logbook, 7).first().steps)
    }

    @Test
    fun showsZeroIntakeAndKeepsMissingDaysEmpty() {
        val date = parseLogMoment("2026-06-14", "10:00")!!
        val logbook = Logbook(selectedDateTime = date,
            foodEntries = listOf(FoodEntry(
                loggedAt = date, name = "Water", servingGrams = 200, calories = 0,
                proteinGrams = 0.0, carbGrams = 0.0, fatGrams = 0.0
            )))
        val month = daysForProgress(logbook, 30)

        assertEquals(30, month.size)
        assertEquals(0, month.last().intakeKcal)
        assertEquals(null, month.first().intakeKcal)
        assertEquals(null, month.first().balanceKcal)
    }

    @Test
    fun buildsProgressDays() {
        val monday = parseLogMoment("2026-06-08", "10:00")!!
        val sunday = parseLogMoment("2026-06-14", "10:00")!!
        val logbook = Logbook(
            selectedDateTime = sunday,
            profile = BodySnapshot(loggedAt = sunday, weightKg = 82.0),
            bodyHistory = listOf(
                BodySnapshot(loggedAt = monday, weightKg = 83.0),
                BodySnapshot(loggedAt = sunday, weightKg = 82.0, watchActiveCalories = 300)
            ),
            foodEntries = listOf(
                FoodEntry(
                    loggedAt = sunday,
                    name = "Sunday bowl",
                    servingGrams = 400,
                    calories = 700,
                    proteinGrams = 42.0,
                    carbGrams = 90.0,
                    fatGrams = 18.0
                )
            ),
            trainingSessions = listOf(
                TrainingSession(
                    loggedAt = sunday,
                    title = "Pull",
                    exercises = listOf(
                        LoggedExercise(
                            name = "Row",
                            sets = listOf(
                                LoggedSet(reps = 10, weightKg = 40.0)
                            )
                        )
                    )
                )
            )
        )

        val progressDays = daysForProgress(logbook, 7)
        val pickedDay = progressDays.last()

        assertEquals(7, progressDays.size)
        assertEquals(700, pickedDay.intakeKcal)
        assertEquals(400.0, pickedDay.trainingVolumeKg ?: 0.0, 0.0)
        assertEquals(1, pickedDay.workoutCount)
        assertEquals(1, pickedDay.setCount)
        assertTrue((pickedDay.burnKcal ?: 0) > 0)
        assertEquals(700 - (pickedDay.burnKcal ?: 0), pickedDay.balanceKcal)
        assertEquals(null, progressDays[1].weightKg)
        assertEquals(null, progressDays[1].balanceKcal)
    }
}
