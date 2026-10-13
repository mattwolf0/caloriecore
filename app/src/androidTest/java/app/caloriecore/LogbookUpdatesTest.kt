package app.caloriecore

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.caloriecore.data.LogbookRepository
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.CalorieSource
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.PlannedSetBlock
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingPlanExercise
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.model.startSheetFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogbookUpdatesTest {
    @Test
    fun editingAPlanKeepsTheSavedWorkoutLink() = withDatabase { context ->
        val repository = LogbookRepository(context)
        try {
            val plan = TrainingPlan(
                title = "Push",
                exercises = listOf(
                    TrainingPlanExercise(name = "Bench", plannedSets = listOf(PlannedSetBlock(weightKg = 40.0)))
                )
            )
            val session = plan.startSheetFor(System.currentTimeMillis())
            val book = Logbook(trainingPlans = listOf(plan), trainingSessions = listOf(session))
            repository.save(book)
            repository.save(book.copy(trainingPlans = listOf(plan.copy(title = "Monday push"))))

            val saved = repository.load()
            assertEquals(plan.id, saved.trainingSessions.single().programId)
            assertEquals(session.exercises.map { it.copy(sessionId = session.id) }, saved.trainingSessions.single().exercises)
            assertEquals("Monday push", saved.trainingPlans.single().title)
        } finally {
            repository.close()
        }
    }

    @Test
    fun bodyUpdatesDoNotRewriteMeals() = withDatabase { context ->
        val repository = LogbookRepository(context)
        try {
            val meal = FoodEntry(
                name = "Bread",
                servingGrams = 100,
                calories = 120,
                proteinGrams = 10.0,
                carbGrams = 15.0,
                fatGrams = 2.0
            )
            val book = Logbook(foodEntries = listOf(meal))
            repository.save(book)
            val db = context.openOrCreateDatabase(DatabaseName, Context.MODE_PRIVATE, null)
            db.execSQL("CREATE TRIGGER keep_meals BEFORE DELETE ON meals BEGIN SELECT RAISE(ABORT, 'meal rewritten'); END")
            db.execSQL("CREATE TRIGGER keep_meal_values BEFORE UPDATE ON meals BEGIN SELECT RAISE(ABORT, 'meal rewritten'); END")
            db.close()

            repository.save(book.copy(bodyHistory = listOf(book.profile.copy(steps = 1200))))
            assertEquals(meal, repository.load().foodEntries.single())
        } finally {
            repository.close()
        }
    }

    @Test
    fun failedInsertRollsBackOtherChanges() = withDatabase { context ->
        val repository = LogbookRepository(context)
        try {
            val book = Logbook(settings = UserPreferences(profileSetupComplete = true))
            repository.save(book)
            val db = context.openOrCreateDatabase(DatabaseName, Context.MODE_PRIVATE, null)
            db.execSQL("CREATE TRIGGER block_food BEFORE INSERT ON meals BEGIN SELECT RAISE(ABORT, 'failed save'); END")
            db.close()
            val meal = FoodEntry(
                name = "Bread",
                servingGrams = 100,
                calories = 120,
                proteinGrams = 10.0,
                carbGrams = 15.0,
                fatGrams = 2.0
            )
            val result = runCatching {
                repository.save(
                    book.copy(
                        foodEntries = listOf(meal),
                        settings = book.settings.copy(usePhoneSteps = false)
                    )
                )
            }
            assertTrue(result.isFailure)
            assertTrue(repository.load().settings.usePhoneSteps)
            assertTrue(repository.load().foodEntries.isEmpty())
        } finally {
            repository.close()
        }
    }

    @Test
    fun keepsTheNewDailySettingsAfterReload() = withDatabase { context ->
        val repository = LogbookRepository(context)
        try {
            val body = BodySnapshot(calorieSource = CalorieSource.Watch, hasWeightEntry = false)
            repository.save(
                Logbook(
                    profile = body,
                    bodyHistory = listOf(body),
                    settings = UserPreferences(profileSetupComplete = true, usePhoneSteps = false)
                )
            )
            val saved = repository.load()
            assertFalse(saved.settings.usePhoneSteps)
            assertEquals(CalorieSource.Watch, saved.bodyHistory.single().calorieSource)
            assertFalse(saved.bodyHistory.single().hasWeightEntry)
        } finally {
            repository.close()
        }
    }

    @Test
    fun versionSevenKeepsMealsAndGetsTheNewColumns() = withDatabase { context ->
        makeVersionFiveDatabase(context)
        val db = context.openOrCreateDatabase(DatabaseName, Context.MODE_PRIVATE, null)
        db.execSQL("ALTER TABLE settings ADD COLUMN profile_setup_complete INTEGER NOT NULL DEFAULT 1")
        db.execSQL(
            "CREATE TABLE activity_entries (id INTEGER PRIMARY KEY, logged_at INTEGER NOT NULL, " +
                "catalog_code TEXT, name TEXT NOT NULL, source TEXT NOT NULL, duration_minutes INTEGER, " +
                "met REAL, weight_kg REAL, active_calories INTEGER NOT NULL)"
        )
        db.version = 7
        db.close()
        val repository = LogbookRepository(context)
        try {
            val saved = repository.load()
            assertEquals("Old meal", saved.foodEntries.single().name)
            assertTrue(saved.settings.profileSetupComplete)
            assertTrue(saved.settings.usePhoneSteps)
        } finally {
            repository.close()
        }
    }

    private fun withDatabase(block: (Context) -> Unit) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DatabaseName)
        try {
            block(context)
        } finally {
            context.deleteDatabase(DatabaseName)
        }
    }
}
