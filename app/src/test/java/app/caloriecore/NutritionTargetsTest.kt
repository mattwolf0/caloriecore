package app.caloriecore

import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.ActivitySource
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.NutritionGoal
import app.caloriecore.ui.model.Sex
import app.caloriecore.ui.model.macroTargetFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionTargetsTest {
    private val profile = BodySnapshot(
        sex = Sex.Male,
        age = 30,
        heightCm = 180,
        weightKg = 80.0,
        sleepHours = 7.0
    )

    @Test
    fun usesDifferentTargetsForEachGoal() {
        val build = macroTargetFor(profile, emptyList(), NutritionGoal.BuildMuscle)
        val lose = macroTargetFor(profile, emptyList(), NutritionGoal.LoseFat)
        val maintain = macroTargetFor(profile, emptyList(), NutritionGoal.MaintainWeight)

        assertEquals(2030, build.calories)
        assertEquals(1380, lose.calories)
        assertEquals(1780, maintain.calories)
        assertEquals(144, build.proteinGrams)
        assertEquals(176, lose.proteinGrams)
        assertEquals(128, maintain.proteinGrams)
        assertEquals(220, build.carbsGrams)
        assertEquals(43, lose.carbsGrams)
        assertEquals(173, maintain.carbsGrams)
    }

    @Test
    fun addsLoggedActivityToTheCalorieTarget() {
        val target = macroTargetFor(
            profile = profile,
            activities = listOf(
                ActivityEntry(
                    name = "Moving boxes",
                    source = ActivitySource.Manual,
                    activeCalories = 200
                )
            ),
            goal = NutritionGoal.MaintainWeight
        )

        assertEquals(1980, target.calories)
        assertTrue(target.carbsGrams > 173)
    }
}
