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

        editor.useOffProduct(
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
        assertTrue(editor.draft.keepsOffPer100Base)
    }

    @Test
    fun manualCaloriesStopProductBase() {
        val editor = foodEditorForTest(selectedDateTime = 100L)

        editor.useOffProduct(FoodProduct(code = "123", name = "Bread", kcalPer100g = 240), "loaded")
        editor.updateCalories("260")

        assertEquals("260", editor.draft.kcal)
        assertFalse(editor.draft.keepsOffPer100Base)
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
        editor.useOffProduct(FoodProduct(code = "999", name = "New yogurt", kcalPer100g = 100), "loaded")

        assertEquals(42L, editor.buildEntry().id)
        assertEquals("New yogurt", editor.buildEntry().name)
    }

    private fun foodEditorForTest(selectedDateTime: Long): FoodEditorState = FoodEditorState(
        draftState = mutableStateOf(FoodDraft(mealLoggedAt = selectedDateTime)),
        lookupNoteState = mutableStateOf(null),
        hasSearchedOffState = mutableStateOf(false),
        offShelfState = mutableStateOf(emptyList())
    )
}
