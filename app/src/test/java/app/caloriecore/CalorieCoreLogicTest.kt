package app.caloriecore

import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.LoggedExercise
import app.caloriecore.ui.model.LoggedSet
import app.caloriecore.ui.model.FoodProduct
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.PlannedSetBlock
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.TrainingPlanExercise
import app.caloriecore.ui.model.Sex
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.burnEstimateForDay
import app.caloriecore.ui.model.sumPlateMacros
import app.caloriecore.ui.model.mealsOnPhoneDay
import app.caloriecore.ui.model.parseLogMoment
import app.caloriecore.ui.model.startSheetFor
import app.caloriecore.ui.text.resolveCalorieCoreLanguage
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieCoreLogicTest {
    @Test
    fun sumsFoodMacros() {
        val foodEntries = listOf(
            FoodEntry(name = "Egg toast", servingGrams = 100, calories = 200, proteinGrams = 20.0, carbGrams = 10.0, fatGrams = 5.0),
            FoodEntry(name = "Rice bowl", servingGrams = 150, calories = 350, proteinGrams = 30.0, carbGrams = 40.0, fatGrams = 10.0)
        )

        val totals = sumPlateMacros(foodEntries)

        assertEquals(550, totals.calories)
        assertEquals(50.0, totals.protein, 0.0)
        assertEquals(50.0, totals.carbs, 0.0)
        assertEquals(15.0, totals.fat, 0.0)
    }

    @Test
    fun productEntryUsesServingGrams() {
        val product = FoodProduct(
            code = "123",
            name = "Ketchup",
            servingGrams = 100,
            kcalPer100g = 120,
            proteinPer100g = 1.2,
            carbsPer100g = 28.0,
            fatPer100g = 0.2,
            source = "openfoodfacts"
        )

        val ketchupServing = product.toFoodEntry(loggedAt = 0L, servingGrams = 25)

        assertEquals(25, ketchupServing.servingGrams)
        assertEquals(30, ketchupServing.calories)
        assertEquals(0.3, ketchupServing.proteinGrams, 0.0001)
        assertEquals(7.0, ketchupServing.carbGrams, 0.0001)
        assertEquals(0.05, ketchupServing.fatGrams, 0.0001)
    }

    @Test
    fun clampsNegativeServing() {
        val ketchup = FoodProduct(name = "Ketchup", kcalPer100g = 120, carbsPer100g = 28.0)

        val entry = ketchup.toFoodEntry(loggedAt = 0L, servingGrams = -25)

        assertEquals(0, entry.servingGrams)
        assertEquals(0, entry.calories)
        assertEquals(0.0, entry.carbGrams, 0.0)
    }

    @Test
    fun calculatesDailyBurn() {
        val bodySnapshot = BodySnapshot(
            sex = Sex.Male,
            age = 30,
            heightCm = 180,
            weightKg = 80.0,
            sleepHours = 7.0,
            watchActiveCalories = 500,
            plannedWorkoutCalories = 300
        )
        val foodEntries = listOf(
            FoodEntry(name = "Training lunch", servingGrams = 100, calories = 1000, proteinGrams = 40.0, carbGrams = 120.0, fatGrams = 20.0)
        )

        val estimate = burnEstimateForDay(bodySnapshot, foodEntries)

        assertEquals(1780, estimate.bmr)
        assertEquals(100, estimate.tef)
        assertEquals(2680, estimate.total)
        assertTrue(estimate.bmi > 24.0)
    }

    @Test
    fun usesBodyFatBmr() {
        val bodySnapshot = BodySnapshot(
            sex = Sex.Male,
            age = 30,
            heightCm = 180,
            weightKg = 80.0,
            bodyFatPercent = 20.0,
            sleepHours = 7.0
        )

        val estimate = burnEstimateForDay(bodySnapshot, emptyList())

        assertEquals(1752, estimate.bmr)
        assertTrue(estimate.usesBodyFatFormula)
    }

    @Test
    fun filtersMealsByDay() {
        val firstDay = parseLogMoment("2026-06-08", "10:00")!!
        val secondDay = parseLogMoment("2026-06-09", "10:00")!!
        val foodEntries = listOf(
            FoodEntry(
                loggedAt = firstDay,
                name = "Breakfast",
                servingGrams = 100,
                calories = 300,
                proteinGrams = 20.0,
                carbGrams = 30.0,
                fatGrams = 8.0
            ),
            FoodEntry(
                loggedAt = secondDay,
                name = "Lunch",
                servingGrams = 100,
                calories = 500,
                proteinGrams = 30.0,
                carbGrams = 60.0,
                fatGrams = 12.0
            )
        )

        val filtered = mealsOnPhoneDay(foodEntries, firstDay)

        assertEquals(1, filtered.size)
        assertEquals("Breakfast", filtered.first().name)
    }

    @Test
    fun sumsWorkoutVolume() {
        val pushSession = TrainingSession(
            title = "Push",
            exercises = listOf(
                LoggedExercise(
                    name = "Bench press",
                    sets = listOf(
                        LoggedSet(reps = 10, weightKg = 30.0),
                        LoggedSet(reps = 12, weightKg = 40.0),
                        LoggedSet(reps = 11, weightKg = 60.0)
                    )
                )
            )
        )

        assertEquals(1440.0, pushSession.totalVolumeKg, 0.0)
        assertEquals(3, pushSession.totalSets)
    }

    @Test
    fun ignoresNegativeSetWeight() {
        val typoSet = LoggedSet(reps = 10, weightKg = -40.0)

        assertEquals(0.0, typoSet.volumeKg, 0.0)
    }

    @Test
    fun expandsPlannedSets() {
        val loggedAt = parseLogMoment("2026-06-09", "18:00")!!
        val planned = PlannedSetBlock(setCount = 3, reps = 10, weightKg = 50.0)

        val sets = planned.expand(loggedAt)

        assertEquals(3, sets.size)
        sets.forEach { set ->
            assertEquals(10, set.reps)
            assertEquals(50.0, set.weightKg, 0.0)
            assertEquals(10, set.plannedReps)
            assertEquals(50.0, set.plannedWeightKg ?: 0.0, 0.0)
        }
    }

    @Test
    fun keepsPlannedSetValues() {
        val loggedAt = parseLogMoment("2026-06-09", "18:00")!!
        val chestPlan = TrainingPlan(
            title = "Chest day",
            exercises = listOf(
                TrainingPlanExercise(
                    name = "Bench press",
                    plannedSets = listOf(PlannedSetBlock(setCount = 3, reps = 10, weightKg = 50.0))
                )
            )
        )

        val chestSession = chestPlan.startSheetFor(loggedAt)
        val editedFirstSet = chestSession.exercises.first().sets.first().copy(reps = 8, weightKg = 55.0)

        assertEquals("Chest day", chestSession.title)
        assertEquals(3, chestSession.totalSets)
        assertEquals(10, editedFirstSet.plannedReps)
        assertEquals(50.0, editedFirstSet.plannedWeightKg ?: 0.0, 0.0)
        assertEquals(8, editedFirstSet.reps)
        assertEquals(55.0, editedFirstSet.weightKg, 0.0)
    }

    @Test
    fun formatsLogNumbers() {
        assertEquals("82 kg", CalorieCoreFormatter.kilograms(82.0))
        assertEquals("82.5 kg", CalorieCoreFormatter.kilograms(82.5))
        assertEquals("42 g", CalorieCoreFormatter.grams(42.0))
        assertEquals("2788 kcal", CalorieCoreFormatter.kcal(2788))
        assertEquals("18%", CalorieCoreFormatter.percent(18.0))
        assertEquals("+2.5 kg", CalorieCoreFormatter.signedDelta(2.5, "kg"))
    }

    @Test
    fun resolvesLanguagePreference() {
        assertEquals(UiLanguage.Hungarian, resolveCalorieCoreLanguage(UiLanguage.System, Locale.forLanguageTag("hu-HU")))
        assertEquals(UiLanguage.German, resolveCalorieCoreLanguage(UiLanguage.System, Locale.forLanguageTag("de-DE")))
        assertEquals(UiLanguage.English, resolveCalorieCoreLanguage(UiLanguage.System, Locale.forLanguageTag("fr-FR")))
        assertEquals(UiLanguage.German, resolveCalorieCoreLanguage(UiLanguage.German, Locale.forLanguageTag("hu-HU")))
    }
}
