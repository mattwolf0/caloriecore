package app.caloriecore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import app.caloriecore.data.LogbookRepository
import app.caloriecore.ui.CalorieCoreApp
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.PlannedSetBlock
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingPlanExercise
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.screens.BodyMetricsEditor
import app.caloriecore.ui.screens.WorkoutLogger
import app.caloriecore.ui.screens.rememberWorkoutDraft
import app.caloriecore.ui.text.calorieCoreStrings
import app.caloriecore.ui.theme.CalorieCoreTheme
import org.junit.Rule
import org.junit.Test

class DraftScreensTest {
    @get:Rule
    val rule = createComposeRule()
    private val strings = calorieCoreStrings(UiLanguage.English)

    @Test
    fun foodDraftSurvivesTabChangeAndStateRestore() = withAppData {
        val restore = StateRestorationTester(rule)
        restore.setContent { CalorieCoreApp() }
        openTab(strings.food)
        rule.onNodeWithText("Bread").performClick()
        input(strings.serving).performScrollTo().performTextReplacement("200")
        input(strings.calories).assertTextContains("240")
        input(strings.protein).performScrollTo().performTextReplacement("22")
        input(strings.calories).assertTextContains("240")
        closeSoftKeyboard()

        openTab(strings.progress)
        openTab(strings.food)
        input(strings.serving).assertTextContains("200")
        input(strings.protein).assertTextContains("22")
        restore.emulateSavedInstanceStateRestore()
        input(strings.protein).assertTextContains("22")
        input(strings.serving).performScrollTo().performTextReplacement("100")
        input(strings.calories).assertTextContains("120")
        input(strings.protein).assertTextContains("11")
    }

    @Test
    fun planDraftKeepsAddedExercisesAfterTabChangeAndRestore() = withAppData {
        val restore = StateRestorationTester(rule)
        restore.setContent { CalorieCoreApp() }
        openTab(strings.train)
        rule.onNodeWithText(strings.createPlan).performScrollTo().performClick()
        input(strings.planName).performScrollTo().performTextReplacement("Push")
        input(strings.planExerciseName).performScrollTo().performTextReplacement("Bench press")
        rule.onNodeWithText(strings.addThisExercise).performScrollTo().performClick()
        closeSoftKeyboard()
        openTab(strings.food)
        openTab(strings.train)
        rule.onNodeWithText("Bench press").assertTextContains("Bench press")
        restore.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Bench press").assertTextContains("Bench press")
        input(strings.planName).assertTextContains("Push")
    }

    @Test
    fun workoutKeepsSetsAndUnfinishedInputsAfterRestore() {
        val restore = StateRestorationTester(rule)
        val plan = TrainingPlan(
            title = "Pull",
            exercises = listOf(
                TrainingPlanExercise(name = "Row", plannedSets = listOf(PlannedSetBlock(setCount = 2, reps = 12, weightKg = 40.0)))
            )
        )
        restore.setContent {
            CalorieCoreTheme(UiThemeMode.System) {
                val draft = rememberWorkoutDraft(100L)
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TextButton(onClick = { draft.startFromPlan(plan, 100L) }) { Text("Start test workout") }
                    Text("Sets: ${draft.setLogScratchpad.sumOf { it.sets.size }}")
                    if (draft.isOpen) WorkoutLogger(draft, strings, {}, {})
                }
            }
        }
        rule.onNodeWithText("Start test workout").performClick()
        rule.onAllNodes(hasSetTextAction() and hasText(strings.reps)).onFirst().performTextClearance()
        rule.onNodeWithText(strings.saveWorkout).assertIsNotEnabled()
        restore.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Sets: 2").assertTextContains("Sets: 2")
        rule.onNodeWithText(strings.saveWorkout).assertIsNotEnabled()
        rule.onAllNodes(hasSetTextAction() and hasText(strings.reps)).onFirst().performTextReplacement("8")
        rule.onAllNodes(hasSetTextAction() and hasText(strings.load)).onFirst().performTextReplacement("42.")
        rule.onAllNodes(hasSetTextAction() and hasText(strings.load)).onFirst().assertTextContains("42.")
        rule.onAllNodes(hasSetTextAction() and hasText(strings.load)).onFirst().performTextInput("5")
        rule.onAllNodes(hasSetTextAction() and hasText(strings.load)).onFirst().assertTextContains("42.5")
        restore.emulateSavedInstanceStateRestore()
        rule.onAllNodes(hasSetTextAction() and hasText(strings.load)).onFirst().assertTextContains("42.5")
    }

    @Test
    fun bodyWeightCanBeClearedAndTypedWithADecimal() {
        rule.setContent {
            CalorieCoreTheme(UiThemeMode.System) {
                var body by rememberSaveable { mutableStateOf(BodySnapshot()) }
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    BodyMetricsEditor(body, strings, { body = it })
                    Text("Saved weight: ${body.weightKg}")
                }
            }
        }
        rule.onNodeWithText(strings.edit).performClick()
        input(strings.weight).performScrollTo().performTextClearance()
        rule.onNodeWithText(strings.saveChanges).assertIsNotEnabled()
        input(strings.weight).performTextInput("82.")
        input(strings.weight).assertTextContains("82.")
        input(strings.weight).performTextInput("5")
        input(strings.weight).assertTextContains("82.5")
        closeSoftKeyboard()
        rule.onNodeWithText(strings.saveChanges).performScrollTo().performClick()
        rule.onNodeWithText("Saved weight: 82.5").assertTextContains("Saved weight: 82.5")
    }

    private fun input(label: String) = rule.onNode(hasSetTextAction() and hasText(label))

    private fun openTab(label: String) {
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(label) and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
        rule.onNode(hasText(label) and hasClickAction()).performClick()
    }

    private fun withAppData(block: () -> Unit) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DatabaseName)
        val repository = LogbookRepository(context)
        repository.save(
            Logbook(
                foodEntries = listOf(
                    FoodEntry(
                        name = "Bread",
                        servingGrams = 100,
                        calories = 120,
                        proteinGrams = 10.0,
                        carbGrams = 15.0,
                        fatGrams = 2.0
                    )
                ),
                settings = UserPreferences(language = UiLanguage.English, profileSetupComplete = true, usePhoneSteps = false)
            )
        )
        repository.close()
        try {
            block()
        } finally {
            context.deleteDatabase(DatabaseName)
        }
    }
}
