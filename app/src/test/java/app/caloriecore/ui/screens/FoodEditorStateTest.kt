package app.caloriecore.ui.screens

import androidx.compose.runtime.mutableStateOf
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.FoodProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodEditorStateTest {
    @Test
    fun updatesProductMacrosForServing() {
        val editor = foodEditorForTest(selectedDateTime = 100L)

        editor.useLookupProduct(
            FoodProduct(
                code = "123",
                name = "Skyr",
                kcalPer100g = 80,
                proteinPer100g = 11.0,
                carbsPer100g = 4.0,
                fatPer100g = 0.2
            ),
            loadedMessage = "loaded"
        )
        editor.updateServing("250")
        editor.refreshProductTotals()

        assertEquals("200", editor.draft.kcal)
        assertEquals("27.5", editor.draft.protein)
        assertEquals("10", editor.draft.carbs)
        assertEquals("0.5", editor.draft.fat)
        assertTrue(editor.draft.keepsProductPer100Base)
    }

    @Test
    fun manualCaloriesChangeOnlyCalories() {
        val editor = foodEditorForTest(selectedDateTime = 100L)

        editor.useLookupProduct(FoodProduct(code = "123", name = "Bread", kcalPer100g = 240), "loaded")
        editor.updateCalories("260")

        assertEquals("260", editor.draft.kcal)
        assertTrue(editor.draft.keepsProductPer100Base)
    }

    @Test
    fun keepsMissingProductMacrosBlank() {
        val editor = foodEditorForTest(selectedDateTime = 100L)

        editor.useLookupProduct(
            FoodProduct(code = "123", name = "Partial yogurt", kcalPer100g = 80),
            "loaded"
        )
        editor.updateServing("250")
        editor.refreshProductTotals()

        assertEquals("200", editor.draft.kcal)
        assertEquals("", editor.draft.protein)
        assertEquals("", editor.draft.carbs)
        assertEquals("", editor.draft.fat)

        val entry = editor.buildEntry()
        assertFalse(entry.proteinKnown)
        assertFalse(entry.carbsKnown)
        assertFalse(entry.fatKnown)
    }

    @Test
    fun keepsEditedMealId() {
        val editor = foodEditorForTest(selectedDateTime = 100L)
        val original = FoodEntry(
            id = 42L,
            loggedAt = 100L,
            name = "Old yogurt",
            servingGrams = 100,
            calories = 90,
            proteinGrams = 8.0,
            carbGrams = 9.0,
            fatGrams = 1.0
        )

        editor.edit(original)
        editor.useLookupProduct(FoodProduct(code = "999", name = "New yogurt", kcalPer100g = 100), "loaded")

        assertEquals(42L, editor.buildEntry().id)
        assertEquals("New yogurt", editor.buildEntry().name)
    }

    @Test
    fun savedMealScalesAllValuesWhenGramsChange() {
        val editor = foodEditorForTest(100L)
        editor.edit(exampleMeal())
        editor.updateServing("200")

        val meal = editor.buildEntry()
        assertEquals(200, meal.servingGrams)
        assertEquals(240, meal.calories)
        assertEquals(20.0, meal.proteinGrams, 0.0)
        assertEquals(30.0, meal.carbGrams, 0.0)
        assertEquals(4.0, meal.fatGrams, 0.0)
        assertEquals(42L, meal.id)
    }

    @Test
    fun manualMacrosOnlyChangeTheirOwnFields() {
        val editor = foodEditorForTest(100L)
        editor.edit(exampleMeal())
        editor.updateProtein("11")
        assertEquals("120", editor.draft.kcal)
        assertEquals("100", editor.draft.grams)
        assertEquals("15", editor.draft.carbs)
        assertEquals("2", editor.draft.fat)

        editor.updateCarbs("16")
        assertEquals("11", editor.draft.protein)
        assertEquals("120", editor.draft.kcal)
        editor.updateFat("3")
        assertEquals("16", editor.draft.carbs)
        assertEquals("100", editor.draft.grams)

        editor.updateServing("200")
        val meal = editor.buildEntry()
        assertEquals(240, meal.calories)
        assertEquals(22.0, meal.proteinGrams, 0.0)
        assertEquals(32.0, meal.carbGrams, 0.0)
        assertEquals(6.0, meal.fatGrams, 0.0)
    }

    @Test
    fun manualMealUsesTheEnteredServingAsItsBase() {
        val editor = foodEditorForTest(100L)
        editor.updateFoodName("Bread")
        editor.updateServing("50")
        editor.updateCalories("120")
        editor.updateProtein("5")
        editor.updateServing("100")
        assertEquals(240, editor.buildEntry().calories)
        assertEquals(10.0, editor.buildEntry().proteinGrams, 0.0)
    }

    @Test
    fun keepsTheLastServingWhileTheGramsFieldIsEmpty() {
        val editor = foodEditorForTest(100L)
        editor.updateFoodName("Bread")
        editor.updateServing("50")
        editor.updateCalories("120")
        editor.updateServing("")
        editor.updateProtein("5")
        editor.updateServing("100")
        assertEquals(240, editor.buildEntry().calories)
        assertEquals(10.0, editor.buildEntry().proteinGrams, 0.0)
    }

    @Test
    fun keepsScaledMacrosReadable() {
        val editor = foodEditorForTest(100L)
        editor.edit(exampleMeal().copy(servingGrams = 300, proteinGrams = 18.4))
        editor.updateServing("200")
        assertEquals("12.2667", editor.draft.protein)
    }

    @Test
    fun servingChangesDoNotLoseSmallMacroValues() {
        val editor = foodEditorForTest(100L)
        editor.edit(exampleMeal().copy(proteinGrams = 0.05))
        assertEquals(0.05, editor.buildEntry().proteinGrams, 0.0)
        editor.updateServing("1")
        editor.updateServing("100")
        assertEquals(0.05, editor.buildEntry().proteinGrams, 0.0)
        assertEquals(120, editor.buildEntry().calories)
    }

    @Test
    fun keepsUnknownMacrosUnknownWhenEditingServing() {
        val editor = foodEditorForTest(100L)
        editor.edit(exampleMeal().copy(fatGrams = 0.0, fatKnown = false))
        editor.updateServing("200")
        assertFalse(editor.buildEntry().fatKnown)
        assertEquals("", editor.draft.fat)
    }

    @Test
    fun invalidNumbersCannotBeSavedAsZeros() {
        val editor = foodEditorForTest(100L)
        editor.edit(exampleMeal())
        editor.updateCalories("999999999999999")
        assertFalse(editor.canSave)
        editor.updateCalories("120")
        editor.updateProtein("1..2")
        assertFalse(editor.canSave)
        editor.updateServing("200")
        assertEquals("1..2", editor.draft.protein)
        assertFalse(editor.canSave)
        editor.updateProtein("-1")
        assertFalse(editor.canSave)
        editor.updateProtein("10")
        editor.updateServing("0")
        assertFalse(editor.canSave)
        editor.updateServing("12.5")
        assertEquals("12.5", editor.draft.grams)
        assertFalse(editor.canSave)
        editor.updateCalories("134.6")
        assertEquals("134.6", editor.draft.kcal)
        assertFalse(editor.canSave)
        editor.updateCalories("120")
        editor.updateServing("")
        assertFalse(editor.canSave)
        editor.updateServing("100")
        assertTrue(editor.canSave)
    }

    @Test
    fun manualCaloriesKeepOtherValuesAndScaleLater() {
        val editor = foodEditorForTest(100L)
        editor.edit(exampleMeal())
        editor.updateCalories("140")
        assertEquals("10", editor.draft.protein)
        assertEquals("15", editor.draft.carbs)
        assertEquals("2", editor.draft.fat)
        assertEquals("100", editor.draft.grams)
        editor.updateServing("200")
        assertEquals(280, editor.buildEntry().calories)
    }

    private fun exampleMeal() = FoodEntry(
        id = 42L,
        loggedAt = 100L,
        name = "Bread",
        servingGrams = 100,
        calories = 120,
        proteinGrams = 10.0,
        carbGrams = 15.0,
        fatGrams = 2.0
    )

    private fun foodEditorForTest(selectedDateTime: Long): FoodEditorState = FoodEditorState(
        draftState = mutableStateOf(FoodDraft(mealLoggedAt = selectedDateTime)),
        lookupNoteState = mutableStateOf(null),
        hasSearchedOffState = mutableStateOf(false),
        offShelfState = mutableStateOf(emptyList())
    )
}
