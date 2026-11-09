package app.caloriecore

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.samePhoneDay
import app.caloriecore.ui.screens.FoodScreen
import app.caloriecore.ui.text.calorieCoreStrings
import app.caloriecore.ui.theme.CalorieCoreTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FoodAddScreenTest {
    @get:Rule
    val rule = createComposeRule()
    private val strings = calorieCoreStrings(UiLanguage.English)
    private val pickedMoment = 1_793_707_200_000L
    private var savedEntry: FoodEntry? = null

    @Test
    fun addFoodShowsFourOptionsInOneRowAndSeparateLookupTools() {
        showFoodScreen()
        rule.onNodeWithText(strings.addMeal).performClick()
        val labels = listOf(strings.customFood, strings.foodBarcode, strings.foodPhoto, strings.foodSearch)
        val bounds = labels.map { label ->
            rule.onNodeWithText(label).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        }
        bounds.forEach { assertEquals(bounds.first().top, it.top, 1f) }
        bounds.zipWithNext().forEach { (left, right) -> assertTrue(left.right <= right.left) }
        rule.onNodeWithText(strings.foodBarcode).performClick()
        rule.onNodeWithText(strings.scanBarcode).assertIsDisplayed()
        input(strings.barcode).assertIsDisplayed()
        rule.onAllNodes(hasSetTextAction() and hasText(strings.foodSearchText)).assertCountEquals(0)

        rule.onNodeWithText(strings.back).performClick()
        rule.onNodeWithText(strings.foodSearch).performClick()
        input(strings.foodSearchText).assertIsDisplayed()
        rule.onAllNodes(hasSetTextAction() and hasText(strings.barcode)).assertCountEquals(0)
        input(strings.foodSearchText).performTextReplacement("a")
        rule.onNodeWithContentDescription(strings.searchFood).assertIsNotEnabled()
    }

    @Test
    fun imageRecognitionOnlyShowsTheComingSoonPage() {
        showFoodScreen()
        rule.onNodeWithText(strings.addMeal).performClick()
        rule.onNodeWithText(strings.foodPhoto).performClick()
        rule.onNodeWithText(strings.comingSoon).assertIsDisplayed()
        rule.onNodeWithText(strings.imageFoodSoon).assertIsDisplayed()
        rule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        assertEquals(null, savedEntry)
        rule.onNodeWithText(strings.back).performClick()
        rule.onNodeWithText(strings.customFood).assertIsDisplayed()
    }

    @Test
    fun customDraftSurvivesRestoreAndSavesToThePickedDay() {
        val restore = StateRestorationTester(rule)
        restore.setContent {
            CalorieCoreTheme(UiThemeMode.Light) {
                FoodScreen(Logbook(selectedDateTime = pickedMoment), strings, {}, { savedEntry = it }, {})
            }
        }
        rule.onNodeWithText(strings.addMeal).performClick()
        rule.onNodeWithText(strings.customFood).performClick()
        input(strings.foodName).performTextReplacement("Toast")
        input(strings.calories).performTextReplacement("120")
        input(strings.protein).performTextReplacement("4.5")
        closeKeyboard()
        restore.emulateSavedInstanceStateRestore()
        input(strings.foodName).assertTextContains("Toast")
        input(strings.protein).assertTextContains("4.5")
        rule.onNodeWithText(strings.back).performClick()
        rule.onNodeWithText(strings.customFood).performClick()
        input(strings.foodName).assertTextContains("Toast")
        sheetAction(strings.addMeal).performScrollTo().performClick()
        rule.runOnIdle {
            val entry = savedEntry
            assertNotNull(entry)
            assertEquals("Toast", entry!!.name)
            assertEquals(120, entry.calories)
            assertEquals(4.5, entry.proteinGrams, 0.001)
            assertTrue(samePhoneDay(pickedMoment, entry.loggedAt))
        }
    }

    @Test
    fun closingAFormStartsTheNextFoodWithAnEmptyDraft() {
        showFoodScreen()
        rule.onNodeWithText(strings.addMeal).performClick()
        rule.onNodeWithText(strings.customFood).performClick()
        input(strings.foodName).performTextReplacement("Toast")
        closeKeyboard()
        rule.onNodeWithContentDescription(strings.close).performClick()
        rule.onNodeWithText(strings.addMeal).performClick()
        rule.onNodeWithText(strings.foodPhoto).assertIsDisplayed()
        rule.onNodeWithText(strings.customFood).performClick()
        input(strings.foodName).assertTextContains("", substring = false)
        sheetAction(strings.addMeal).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun anExistingFoodOpensItsDetailsAndKeepsItsId() {
        val entry = FoodEntry(
            id = 42L,
            loggedAt = pickedMoment,
            name = "Bread",
            servingGrams = 100,
            calories = 120,
            proteinGrams = 10.0,
            carbGrams = 15.0,
            fatGrams = 2.0
        )
        showFoodScreen(listOf(entry))
        rule.onNodeWithText(entry.name).performClick()
        input(strings.serving).performTextReplacement("200")
        input(strings.calories).assertTextContains("240")
        closeKeyboard()
        sheetAction(strings.updateMeal).performScrollTo().performClick()
        rule.runOnIdle {
            assertEquals(entry.id, savedEntry?.id)
            assertEquals(240, savedEntry?.calories)
        }
    }

    private fun showFoodScreen(entries: List<FoodEntry> = emptyList()) {
        rule.setContent {
            CalorieCoreTheme(UiThemeMode.Light) {
                FoodScreen(
                    logbook = Logbook(selectedDateTime = pickedMoment, foodEntries = entries),
                    strings = strings,
                    onSelectedDateTimeChange = {},
                    onSaveFoodEntry = { savedEntry = it },
                    onRemoveFoodEntry = {}
                )
            }
        }
    }

    private fun input(label: String) = rule.onNode(hasSetTextAction() and hasText(label))

    private fun closeKeyboard() {
        onView(isRoot()).inRoot(RootMatchers.isDialog()).perform(closeSoftKeyboard())
    }

    private fun sheetAction(label: String) = rule.onNode(hasText(label) and hasClickAction() and hasAnyAncestor(isDialog()))
}
