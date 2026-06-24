package app.caloriecore.ui.screens

import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.LoggedExercise
import app.caloriecore.ui.model.LoggedSet
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.parseLogMoment
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
        assertEquals(400.0, pickedDay.trainingVolumeKg, 0.0)
        assertTrue(pickedDay.burnKcal > 0)
        assertEquals(700 - pickedDay.burnKcal, pickedDay.balanceKcal)
    }
}
