package app.caloriecore

import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.ActivitySource
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.model.withClearedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogbookResetTest {
    @Test
    fun clearsLogsAndReturnsToProfileSetup() {
        val logbook = Logbook(
            foodEntries = listOf(FoodEntry(name = "Oats", servingGrams = 100, calories = 380,
                proteinGrams = 13.0, carbGrams = 60.0, fatGrams = 7.0)),
            activityEntries = listOf(ActivityEntry(name = "Walk", source = ActivitySource.Manual, activeCalories = 100)),
            trainingPlans = listOf(TrainingPlan(title = "Push")),
            trainingSessions = listOf(TrainingSession(title = "Push")),
            settings = UserPreferences(UiLanguage.German, UiThemeMode.Dark, profileSetupComplete = true)
        )
        val cleared = logbook.withClearedData()
        assertTrue(cleared.foodEntries.isEmpty())
        assertTrue(cleared.activityEntries.isEmpty())
        assertTrue(cleared.trainingPlans.isEmpty())
        assertTrue(cleared.trainingSessions.isEmpty())
        assertTrue(cleared.bodyHistory.isEmpty())
        assertFalse(cleared.settings.profileSetupComplete)
        assertEquals(UiLanguage.German, cleared.settings.language)
        assertEquals(UiThemeMode.Dark, cleared.settings.themeMode)
    }
}
