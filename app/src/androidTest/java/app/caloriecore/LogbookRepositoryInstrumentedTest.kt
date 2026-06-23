package app.caloriecore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.caloriecore.data.LogbookRepository
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.LoggedExercise
import app.caloriecore.ui.model.LoggedSet
import app.caloriecore.ui.model.PlannedSetBlock
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingPlanExercise
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.parseLogMoment
import app.caloriecore.ui.model.startSheetFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogbookRepositoryInstrumentedTest {
    @Test
    fun savesAndLoadsLocalData() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DatabaseName)
        try {
            val repository = LogbookRepository(context)
            val loggedAt = parseLogMoment("2026-06-08", "18:30")!!
            val bodySnapshot = BodySnapshot(
                loggedAt = loggedAt,
                weightKg = 84.0,
                bodyFatPercent = 18.0,
                steps = 8500
            )
            val riceBowl = FoodEntry(
                loggedAt = loggedAt,
                name = "Rice bowl",
                servingGrams = 420,
                calories = 650,
                proteinGrams = 42.0,
                carbGrams = 82.0,
                fatGrams = 18.0
            )
            val mondayPressPlan = TrainingPlan(
                title = "Monday press",
                focus = "",
                daysPerWeek = 2,
                exercises = listOf(
                    TrainingPlanExercise(
                        name = "Bench press",
                        target = "",
                        plannedSets = listOf(
                            PlannedSetBlock(setCount = 3, reps = 10, weightKg = 50.0)
                        )
                    )
                )
            )
            val pressSession = mondayPressPlan.startSheetFor(loggedAt).copy(
                exercises = listOf(
                    mondayPressPlan.startSheetFor(loggedAt).exercises.first().copy(
                        sets = listOf(
                            LoggedSet(
                                loggedAt = loggedAt,
                                reps = 10,
                                weightKg = 50.0,
                                plannedReps = 10,
                                plannedWeightKg = 50.0
                            ),
                            LoggedSet(
                                loggedAt = loggedAt,
                                reps = 9,
                                weightKg = 55.0,
                                plannedReps = 10,
                                plannedWeightKg = 50.0
                            )
                        )
                    )
                )
            )

            repository.save(
                Logbook(
                    selectedDateTime = loggedAt,
                    profile = bodySnapshot,
                    bodyHistory = listOf(bodySnapshot),
                    foodEntries = listOf(riceBowl),
                    trainingSessions = listOf(pressSession),
                    trainingPlans = listOf(mondayPressPlan)
                )
            )

            val savedLogbook = repository.load()

            assertEquals(UiLanguage.System, savedLogbook.settings.language)
            assertEquals(UiThemeMode.System, savedLogbook.settings.themeMode)
            assertEquals(1, savedLogbook.bodyHistory.size)
            assertEquals(18.0, savedLogbook.bodyHistory.first().bodyFatPercent ?: 0.0, 0.0)
            assertEquals(listOf("Rice bowl"), savedLogbook.foodEntries.map { it.name })
            assertEquals(listOf("Monday press"), savedLogbook.trainingPlans.map { it.title })
            assertEquals(3, savedLogbook.trainingPlans.first().exercises.first().plannedSets.first().setCount)
            assertEquals(10, savedLogbook.trainingPlans.first().exercises.first().plannedSets.first().reps)
            assertEquals(50.0, savedLogbook.trainingPlans.first().exercises.first().plannedSets.first().weightKg, 0.0)
            assertEquals(1, savedLogbook.trainingSessions.size)
            assertEquals(2, savedLogbook.trainingSessions.first().totalSets)
            assertEquals(995.0, savedLogbook.trainingSessions.first().totalVolumeKg, 0.0)
            assertEquals(10, savedLogbook.trainingSessions.first().exercises.first().sets.first().plannedReps)
            assertEquals(
                50.0,
                savedLogbook.trainingSessions.first().exercises.first().sets.first().plannedWeightKg ?: 0.0,
                0.0
            )
            assertTrue(savedLogbook.trainingPlans.none { it.title in setOf("Push Pull Legs", "Full Body 3", "Upper Lower") })
        } finally {
            context.deleteDatabase(DatabaseName)
        }
    }

    @Test
    fun migratesOldSettingsToSystem() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DatabaseName)
        try {
            makeOldVersionThreeDatabase(context)

            val migratedLogbook = LogbookRepository(context).load()

            assertEquals(UiLanguage.System, migratedLogbook.settings.language)
            assertEquals(UiThemeMode.System, migratedLogbook.settings.themeMode)
        } finally {
            context.deleteDatabase(DatabaseName)
        }
    }
}
