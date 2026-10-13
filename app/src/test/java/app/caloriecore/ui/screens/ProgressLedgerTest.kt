package app.caloriecore.ui.screens

import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.ActivitySource
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.LoggedExercise
import app.caloriecore.ui.model.LoggedSet
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.parseLogMoment
import app.caloriecore.ui.model.pickedDayReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressLedgerTest {
    @Test
    fun buildsWeekLedger() {
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

        val week = weekAroundPickedDay(logbook, 7)
        val pickedDay = week.last()

        assertEquals(7, week.size)
        assertEquals(700, pickedDay.intakeKcal)
        assertEquals(400.0, pickedDay.trainingVolumeKg ?: 0.0, 0.0)
        assertTrue((pickedDay.burnKcal ?: 0) > 0)
        assertEquals(700 - (pickedDay.burnKcal ?: 0), pickedDay.balanceKcal)
        assertEquals(null, week[1].weightKg)
        assertEquals(null, week[1].balanceKcal)
    }

    @Test
    fun includesActivityInTheSameWayAsTheDailyReport() {
        val day = parseLogMoment("2026-10-13", "12:00")!!
        val body = BodySnapshot(loggedAt = day, sleepHours = 7.0)
        val book = Logbook(
            selectedDateTime = day,
            profile = body,
            bodyHistory = listOf(body),
            foodEntries = listOf(
                FoodEntry(
                    loggedAt = day,
                    name = "Lunch",
                    servingGrams = 100,
                    calories = 120,
                    proteinGrams = 10.0,
                    carbGrams = 15.0,
                    fatGrams = 2.0
                )
            ),
            activityEntries = listOf(
                ActivityEntry(
                    loggedAt = day,
                    name = "Walk",
                    source = ActivitySource.Manual,
                    activeCalories = 300
                )
            )
        )
        val point = weekAroundPickedDay(book, 7).last()
        assertEquals(book.pickedDayReport().burnEstimate.total, point.burnKcal)
        assertEquals(300, book.pickedDayReport().burnEstimate.loggedActivityCalories)
    }

    @Test
    fun stepUpdatesDoNotAddWeightPoints() {
        val yesterday = parseLogMoment("2026-10-12", "12:00")!!
        val today = parseLogMoment("2026-10-13", "12:00")!!
        val oldBody = BodySnapshot(loggedAt = yesterday, weightKg = 82.0)
        val stepUpdate = oldBody.copy(id = 100L, loggedAt = today, steps = 3000, hasWeightEntry = false)
        val book = Logbook(selectedDateTime = today, profile = stepUpdate, bodyHistory = listOf(oldBody, stepUpdate))
        assertEquals(null, weekAroundPickedDay(book, 7).last().weightKg)
    }

    @Test
    fun dailyReportAndChartUseTheLastBodyDataForTheDay() {
        val noon = parseLogMoment("2026-10-13", "12:00")!!
        val evening = parseLogMoment("2026-10-13", "18:00")!!
        val early = BodySnapshot(loggedAt = noon, weightKg = 82.0, plannedWorkoutCalories = 100)
        val late = early.copy(id = 100L, loggedAt = evening, weightKg = 80.0, plannedWorkoutCalories = 300)
        val food = FoodEntry(
            loggedAt = noon,
            name = "Lunch",
            servingGrams = 100,
            calories = 120,
            proteinGrams = 10.0,
            carbGrams = 15.0,
            fatGrams = 2.0
        )
        val book = Logbook(
            selectedDateTime = noon,
            profile = early,
            bodyHistory = listOf(early, late),
            foodEntries = listOf(food)
        )

        assertEquals(300, book.pickedDayReport().burnEstimate.plannedTraining)
        assertEquals(book.pickedDayReport().burnEstimate.total, weekAroundPickedDay(book, 7).last().burnKcal)
    }
}
