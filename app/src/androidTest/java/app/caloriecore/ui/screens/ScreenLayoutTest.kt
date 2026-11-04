package app.caloriecore.ui.screens

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.caloriecore.ui.BottomDock
import app.caloriecore.ui.components.ClickableLogCard
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.ActivitySource
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.LoggedExercise
import app.caloriecore.ui.model.LoggedSet
import app.caloriecore.ui.model.NutritionGoal
import app.caloriecore.ui.model.PhoneStepState
import app.caloriecore.ui.model.PhoneStepStatus
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.phoneNowMillis
import app.caloriecore.ui.navigation.CalorieCoreTab
import app.caloriecore.ui.text.englishStrings
import app.caloriecore.ui.text.germanStrings
import app.caloriecore.ui.text.hungarianStrings
import app.caloriecore.ui.theme.CalorieCoreTheme
import app.caloriecore.ui.theme.UiSpacing
import java.io.File
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenLayoutTest {
    @get:Rule
    val screen = createComposeRule()

    @Test
    fun todayShowsTotalsAndQuickActions() {
        val logbook = sampleLogbook()
        var foodOpened = false
        showScreen {
            Scaffold(bottomBar = {
                BottomDock(CalorieCoreTab.Today, hungarianStrings::dockLabel, {})
            }) { padding ->
                Box(Modifier.padding(padding)) {
                    TodayScreen(
                        logbook, hungarianStrings, {}, {},
                        PhoneStepState(6420, PhoneStepStatus.Active),
                        onOpenFood = { foodOpened = true }, onOpenTraining = {}
                    )
                }
            }
        }
        screen.onNodeWithText(hungarianStrings.balanceHint).assertIsDisplayed()
        screen.onAllNodesWithText("6420")[0].assertIsDisplayed()
        saveScreen("today-light")
        screen.onNodeWithText(hungarianStrings.addMeal).performScrollTo().performClick()
        screen.runOnIdle { assertEquals(true, foodOpened) }
    }

    @Test
    fun progressSwitchesToThirtyDays() {
        showScreen(theme = UiThemeMode.Dark) {
            ProgressScreen(sampleLogbook(), englishStrings, {})
        }
        screen.onNode(hasText(englishStrings.food) and hasClickAction()).assertIsSelected()
        saveScreen("progress-dark")
        screen.onNodeWithText(englishStrings.monthWindow).performScrollTo().performClick().assertIsSelected()
        screen.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("‹ ${englishStrings.previousDay}"))
        screen.onAllNodesWithText("‹ ${englishStrings.previousDay}")[0].performClick()
        screen.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(englishStrings.bodyStats) and hasClickAction())
        screen.onNode(hasText(englishStrings.bodyStats) and hasClickAction()).performClick()
        screen.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("80.2 kg"))
        screen.onNodeWithText("80.2 kg").assertIsDisplayed()
        saveScreen("weight-month")
    }

    @Test
    fun chartKeepsMissingDaysSeparateFromZero() {
        val progressDays = daysForProgress(sampleLogbook(), 7)
        showScreen {
            var picked by remember { mutableIntStateOf(6) }
            ProgressChartCard(
                "Calories", progressDays,
                listOf(ChartLine("Food", listOf(null, null, null, null, null, null, 0.0), Color.Green)),
                "kcal", englishStrings, pickedIndex = picked, onSelectDay = { picked = it }
            )
        }
        screen.onNodeWithText("0 kcal").assertIsDisplayed()
        screen.onNodeWithContentDescription(englishStrings.previousDay).performClick()
        screen.onNodeWithText("—").assertIsDisplayed()
        screen.onNodeWithContentDescription(englishStrings.nextDay).performClick()
        screen.onNodeWithText("0 kcal").assertIsDisplayed()
    }

    @Test
    fun progressKeepsUsefulChartsInGroups() {
        showScreen { ProgressScreen(sampleLogbook(), englishStrings, {}) }
        val list = screen.onNode(hasScrollToNodeAction())
        list.performScrollToNode(hasText(englishStrings.energyTrend))
        screen.onNodeWithText(englishStrings.energyTrend).assertIsDisplayed()
        saveScreen("progress-food")

        list.performScrollToNode(hasText(englishStrings.bodyStats) and hasClickAction())
        screen.onNode(hasText(englishStrings.bodyStats) and hasClickAction()).performClick()
        list.performScrollToNode(hasText(englishStrings.bodyFat))
        screen.onAllNodesWithText(englishStrings.bodyFat)[0].assertIsDisplayed()
        saveScreen("progress-body")

        list.performScrollToNode(hasText(englishStrings.activity) and hasClickAction())
        screen.onNode(hasText(englishStrings.activity) and hasClickAction()).performClick()
        list.performScrollToNode(hasText(englishStrings.trainingTrend))
        screen.onNodeWithText(englishStrings.trainingTrend).assertIsDisplayed()
        saveScreen("progress-activity")
    }

    @Test
    fun settingsSavesTheNutritionGoal() {
        var savedGoal: NutritionGoal? = null
        showScreen {
            SettingsScreen(
                Logbook(),
                englishStrings,
                onSettingsChange = { savedGoal = it.nutritionGoal },
                onReset = {}
            )
        }
        screen.onNode(hasText(englishStrings.goal) and hasClickAction()).performClick()
        screen.onNode(hasText(englishStrings.loseFat) and hasClickAction()).performClick()
        screen.runOnIdle { assertEquals(NutritionGoal.LoseFat, savedGoal) }
    }

    @Test
    fun macroTargetsStayReadableWithLargeText() {
        showScreen(width = 320.dp, fontScale = 1.5f) {
            TodayScreen(sampleLogbook(), englishStrings, {}, {}, PhoneStepState(), {}, {})
        }
        screen.onNodeWithText(englishStrings.dailyMacros).performScrollTo().assertIsDisplayed()
        screen.onNodeWithText("${englishStrings.macroTarget}: ${englishStrings.maintainWeight}")
            .assertIsDisplayed()
        screen.onNodeWithText(englishStrings.protein).performScrollTo().assertIsDisplayed()
        screen.onNodeWithText(englishStrings.carbs).performScrollTo().assertIsDisplayed()
        screen.onNodeWithText(englishStrings.fat).performScrollTo().assertIsDisplayed()
        saveScreen("macro-targets-large")
    }

    @Test
    fun monthChartCanPickBothEndsAndOpenTheDay() {
        val progressDays = daysForProgress(sampleLogbook(), 30)
        val picked = mutableIntStateOf(29)
        var opened: Long? = null
        showScreen {
            Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                ProgressChartCard(
                    "Steps", progressDays, listOf(ChartLine("Count", (1..30).map { it.toDouble() }, Color.Green)),
                    "", englishStrings, picked.intValue, { picked.intValue = it }, onOpenDay = { opened = it }
                )
            }
        }
        val chart = screen.onNodeWithContentDescription("Steps. Count", substring = true)
        chart.performTouchInput { click(Offset(1f, center.y)) }
        screen.runOnIdle { assertEquals(0, picked.intValue) }
        screen.onNodeWithText(englishStrings.openDay).performScrollTo().performClick()
        screen.runOnIdle { assertEquals(progressDays.first().millis, opened) }
        chart.performScrollTo()
        val width = chart.fetchSemanticsNode().boundsInRoot.width
        chart.performTouchInput { click(Offset(width - 1f, center.y)) }
        screen.runOnIdle { assertEquals(29, picked.intValue) }
        saveScreen("progress-month")
    }

    @Test
    fun progressGroupsStayVisibleWithLargeText() {
        showScreen(width = 320.dp, fontScale = 1.5f, theme = UiThemeMode.Dark) {
            ProgressScreen(sampleLogbook(), germanStrings, {})
        }
        listOf(germanStrings.food, germanStrings.bodyStats, germanStrings.activity).forEach { label ->
            screen.onNode(hasText(label) and hasClickAction()).assertIsDisplayed().performClick().assertIsSelected()
        }
        saveScreen("progress-groups-large")
        screen.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(germanStrings.trainingTrend))
        screen.onNodeWithText(germanStrings.trainingTrend).assertIsDisplayed()
    }

    @Test
    fun chartShowsAnEmptyMessageWithoutEntries() {
        showScreen {
            ProgressChartCard(
                "Weight", daysForProgress(Logbook(bodyHistory = emptyList()), 7),
                listOf(ChartLine("Weight", List(7) { null }, Color.Green)), "kg", englishStrings,
                pickedIndex = 6, onSelectDay = {}
            )
        }
        screen.onNodeWithText(englishStrings.chartEmpty).assertIsDisplayed()
    }

    @Test
    fun foodSearchKeepsBothInputsOnSmallScreens() {
        showScreen(width = 320.dp, fontScale = 1.5f) {
            FoodScreen(Logbook(), germanStrings, {}, {}, {}, openEditorOnStart = true)
        }
        val options = listOf(germanStrings.customFood, germanStrings.foodBarcode, germanStrings.foodPhoto, germanStrings.foodSearch)
            .map { label -> screen.onNode(hasText(label) and hasClickAction()).assertIsDisplayed().fetchSemanticsNode().boundsInRoot }
        options.zipWithNext { left, right ->
            assertEquals(left.top, right.top, 1f)
            assertTrue(left.right <= right.left)
        }
        saveScreen("food-options-row")
        screen.onNodeWithText(germanStrings.foodPhoto).performClick()
        screen.onNodeWithText(germanStrings.comingSoon).assertIsDisplayed()
        screen.onNode(hasSetTextAction()).assertDoesNotExist()
        screen.onNodeWithText(germanStrings.foodSearch).performClick()
        screen.onNode(hasSetTextAction()).performTextInput("Haferflocken")
        Espresso.closeSoftKeyboard()
        screen.onNodeWithText(germanStrings.foodBarcode).performScrollTo().performClick()
        screen.onNode(hasSetTextAction()).performTextInput("1234567890123")
        Espresso.closeSoftKeyboard()
        screen.onNodeWithText(germanStrings.findProductByBarcode).performScrollTo().assertIsDisplayed()
        saveScreen("food-small-german")
        screen.onNodeWithText(germanStrings.foodSearch).performScrollTo().performClick()
        screen.onNodeWithText("Haferflocken").assertIsDisplayed()
        screen.onNodeWithText(germanStrings.foodBarcode).performScrollTo().performClick()
        screen.onNodeWithText("1234567890123").assertIsDisplayed()
    }

    @Test
    fun foodShortcutOpensTheEditor() {
        showScreen(width = 320.dp) {
            FoodScreen(Logbook(), englishStrings, {}, {}, {}, openEditorOnStart = true)
        }
        screen.onNodeWithText(englishStrings.customFood).performClick()
        screen.onNodeWithText(englishStrings.foodName).performScrollTo().performTextInput("Oats")
        screen.onNodeWithText(englishStrings.calories).performScrollTo().performTextInput("150")
        screen.onNode(hasText(englishStrings.fat) and hasSetTextAction()).performScrollTo().assertIsDisplayed()
        saveScreen("food-editor-small")
    }

    @Test
    fun trainingOpensTheActivityEditor() {
        showScreen(width = 320.dp, fontScale = 1.5f) {
            TrainScreen(Logbook(), germanStrings, {}, emptyList(), UiLanguage.German, {}, {}, {}, {}, {}, {})
        }
        screen.onNodeWithText(germanStrings.addActivity).performScrollTo().performClick()
        screen.onNodeWithText(germanStrings.otherActivity).performScrollTo().performClick()
        screen.onNodeWithText(germanStrings.cancel).performScrollTo().assertIsDisplayed()
        saveScreen("activity-small-german")
    }

    @Test
    fun settingsAsksBeforeClearingData() {
        var cleared = false
        showScreen(width = 320.dp, fontScale = 1.5f) {
            SettingsScreen(Logbook(), germanStrings, {}, { cleared = true })
        }
        screen.onNodeWithText(germanStrings.resetData).performScrollTo().performClick()
        screen.onNodeWithText(germanStrings.clearDataConfirmTitle).assertIsDisplayed()
        screen.runOnIdle { assertEquals(false, cleared) }
        screen.onNodeWithText(germanStrings.cancel).performClick()
        screen.runOnIdle { assertEquals(false, cleared) }
        saveScreen("settings-small-german")
    }

    @Test
    fun foodFieldsShareARowAtNormalTextSize() {
        showScreen {
            FoodScreen(Logbook(), englishStrings, {}, {}, {}, openEditorOnStart = true)
        }
        screen.onNodeWithText(englishStrings.customFood).performClick()
        screen.onNodeWithText(englishStrings.serving).performScrollTo()
        val serving = screen.onNodeWithText(englishStrings.serving).fetchSemanticsNode().boundsInRoot
        val calories = screen.onNodeWithText(englishStrings.calories).fetchSemanticsNode().boundsInRoot
        assertEquals(serving.top, calories.top, 1f)
        assertTrue(calories.left >= serving.right)
        saveScreen("food-fields-fixed")
    }

    @Test
    fun foodBackKeepsDraftUntilDiscarded() {
        showScreen {
            FoodScreen(Logbook(), englishStrings, {}, {}, {}, openEditorOnStart = true)
        }
        screen.onNodeWithText(englishStrings.customFood).performClick()
        screen.onNodeWithText(englishStrings.foodName).performScrollTo().performTextInput("Oats")
        Espresso.closeSoftKeyboard()
        Espresso.pressBack()
        screen.onNodeWithText(englishStrings.discardChanges).assertIsDisplayed()
        screen.onNodeWithText(englishStrings.keepEditing).performClick()
        screen.onNodeWithText("Oats").assertExists()
        screen.onNodeWithText(englishStrings.cancel).performScrollTo().performClick()
        screen.onNodeWithText(englishStrings.discard).performClick()
        screen.onNodeWithText(englishStrings.customFood).assertDoesNotExist()
        screen.onNodeWithText(englishStrings.foodTitle).assertIsDisplayed()
    }

    @Test
    fun profileOnlySavesAfterPressingSave() {
        var saved: BodySnapshot? = null
        val logbook = sampleLogbook()
        showScreen {
            TodayScreen(logbook, englishStrings, {}, { saved = it }, PhoneStepState(), {}, {})
        }
        screen.onNodeWithText(englishStrings.edit).performScrollTo().performClick()
        screen.onNodeWithText(englishStrings.weight).performScrollTo().performTextClearance()
        screen.onNodeWithText(englishStrings.weight).performTextInput("80.")
        screen.onNodeWithText(englishStrings.weight).performTextInput("5")
        screen.runOnIdle { assertEquals(null, saved) }
        screen.onNodeWithText(englishStrings.weight).assertTextContains("80.5")
        Espresso.closeSoftKeyboard()
        screen.onNodeWithText(englishStrings.save).performScrollTo().performClick()
        screen.runOnIdle { assertEquals(80.5, saved!!.weightKg, 0.0) }
    }

    @Test
    fun profileCancelKeepsSavedValues() {
        var saved = false
        showScreen {
            TodayScreen(sampleLogbook(), englishStrings, {}, { saved = true }, PhoneStepState(), {}, {})
        }
        screen.onNodeWithText(englishStrings.edit).performScrollTo().performClick()
        screen.onNodeWithText(englishStrings.age).performScrollTo().performTextReplacement("35")
        Espresso.closeSoftKeyboard()
        screen.onNodeWithText(englishStrings.cancel).performScrollTo().performClick()
        screen.onNodeWithText(englishStrings.discard).performClick()
        screen.runOnIdle { assertEquals(false, saved) }
    }

    @Test
    fun editorHidesNavigationAndRestoresItOnClose() {
        showScreen {
            var isEditorOpen by remember { mutableStateOf(false) }
            Scaffold(bottomBar = {
                if (!isEditorOpen) BottomDock(CalorieCoreTab.Food, englishStrings::dockLabel, {})
            }) { padding ->
                Box(Modifier.padding(padding)) {
                    FoodScreen(Logbook(), englishStrings, {}, {}, {}, onEditorOpenChange = { isEditorOpen = it })
                }
            }
        }
        screen.onNodeWithText(englishStrings.addMeal).performScrollTo().performClick()
        screen.onNodeWithText(englishStrings.settings).assertDoesNotExist()
        screen.onNodeWithText(englishStrings.cancel).performScrollTo().performClick()
        screen.onNodeWithText(englishStrings.settings).assertIsDisplayed()
    }

    @Test
    fun logsWorkoutWithoutCreatingAPlan() {
        var saved: TrainingSession? = null
        showScreen {
            TrainScreen(Logbook(), englishStrings, {}, emptyList(), UiLanguage.English,
                {}, {}, {}, {}, { saved = it }, {})
        }
        screen.onNodeWithText(englishStrings.startWorkout).performScrollTo().performClick()
        screen.onNodeWithText(englishStrings.planExerciseName).performScrollTo().performTextInput("Push up")
        Espresso.closeSoftKeyboard()
        screen.onNodeWithText(englishStrings.addPlanExercise).performScrollTo().performClick()
        screen.onNodeWithText(englishStrings.addSet).performScrollTo().performClick()
        screen.onNodeWithText(englishStrings.saveWorkout).performScrollTo().performClick()
        screen.runOnIdle {
            assertEquals(null, saved!!.programId)
            assertEquals(1, saved!!.totalSets)
            assertEquals("Push up", saved!!.exercises.first().name)
        }
    }

    @Test
    fun anEmptyFoodDayDoesNotShowADeficit() {
        showScreen {
            TodayScreen(Logbook(), englishStrings, {}, {}, PhoneStepState(), {}, {})
        }
        screen.onNodeWithText(englishStrings.noMealsLogged).assertIsDisplayed()
        screen.onNodeWithText(englishStrings.balanceHint).assertDoesNotExist()
        saveScreen("today-empty-fixed")
    }

    @Test
    fun workoutWeightKeepsDecimalsWhileTyping() {
        var saved: TrainingSession? = null
        showScreen {
            TrainScreen(Logbook(), englishStrings, {}, emptyList(), UiLanguage.English,
                {}, {}, {}, {}, { saved = it }, {})
        }
        screen.onNodeWithText(englishStrings.startWorkout).performScrollTo().performClick()
        screen.onNodeWithText(englishStrings.planExerciseName).performScrollTo().performTextInput("Bench press")
        Espresso.closeSoftKeyboard()
        screen.onNodeWithText(englishStrings.addPlanExercise).performScrollTo().performClick()
        screen.onNodeWithText(englishStrings.addSet).performScrollTo().performClick()
        val weight = screen.onAllNodes(hasText(englishStrings.load) and hasSetTextAction())[0]
        weight.performScrollTo().performTextClearance()
        weight.performTextInput("80.")
        weight.performTextInput("5")
        weight.assertTextContains("80.5")
        val reps = screen.onAllNodes(hasText(englishStrings.reps) and hasSetTextAction())[0]
        reps.performScrollTo().performTextClearance()
        Espresso.closeSoftKeyboard()
        screen.onNodeWithText(englishStrings.saveWorkout).performScrollTo().assertIsNotEnabled()
        reps.performScrollTo().performTextInput("8")
        Espresso.closeSoftKeyboard()
        weight.performScrollTo()
        saveScreen("workout-fields-fixed")
        screen.onNodeWithText(englishStrings.saveWorkout).performScrollTo().performClick()
        screen.runOnIdle {
            val set = saved!!.exercises.first().sets.first()
            assertEquals(80.5, set.weightKg, 0.0)
            assertEquals(8, set.reps)
        }
    }

    @Test
    fun largeTextKeepsAllNavigationLabelsReadable() {
        showScreen(width = 320.dp, fontScale = 1.5f, theme = UiThemeMode.Dark) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                BottomDock(CalorieCoreTab.Today, germanStrings::dockLabel, {})
            }
        }
        saveScreen("navigation-large-german")
        CalorieCoreTab.entries.forEach { tab ->
            val layout = mutableListOf<TextLayoutResult>()
            val label = screen.onNodeWithText(germanStrings.dockLabel(tab), useUnmergedTree = true)
            label
                .assertIsDisplayed()
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layout) }
            assertTrue(layout.isNotEmpty())
            assertEquals(1, layout.first().lineCount)
            assertEquals(false, layout.first().isLineEllipsized(0))
            val textWidth = layout.first().getLineRight(0) - layout.first().getLineLeft(0)
            assertTrue(textWidth <= label.fetchSemanticsNode().boundsInRoot.width + 1f)
        }
    }

    @Test
    fun pageTitlesUseTheSameSizeAndMargins() {
        val selected = mutableStateOf(CalorieCoreTab.Today)
        val strings = englishStrings
        val logbook = sampleLogbook()
        showScreen {
            Scaffold(bottomBar = { BottomDock(selected.value, strings::dockLabel, {}) }) { padding ->
                Box(Modifier.padding(padding)) {
                    when (selected.value) {
                        CalorieCoreTab.Today -> TodayScreen(logbook, strings, {}, {}, PhoneStepState(), {}, {})
                        CalorieCoreTab.Food -> FoodScreen(logbook, strings, {}, {}, {})
                        CalorieCoreTab.Train -> TrainScreen(logbook, strings, {}, emptyList(), UiLanguage.English, {}, {}, {}, {}, {}, {})
                        CalorieCoreTab.Progress -> ProgressScreen(logbook, strings, {})
                        CalorieCoreTab.Settings -> SettingsScreen(logbook, strings, {}, {})
                    }
                }
            }
        }
        val titles = mapOf(
            CalorieCoreTab.Today to strings.todayTitle,
            CalorieCoreTab.Food to strings.foodTitle,
            CalorieCoreTab.Train to strings.trainTitle,
            CalorieCoreTab.Progress to strings.progressTitle,
            CalorieCoreTab.Settings to strings.settingsTitle
        )
        var firstLeft: Float? = null
        var firstTop: Float? = null
        titles.forEach { (tab, title) ->
            screen.runOnIdle { selected.value = tab }
            val node = screen.onNode(hasText(title) and !hasAnyAncestor(hasClickAction()), useUnmergedTree = true)
            val layout = mutableListOf<TextLayoutResult>()
            node.assertIsDisplayed().performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layout) }
            assertEquals(24.sp, layout.single().layoutInput.style.fontSize)
            val bounds = node.fetchSemanticsNode().boundsInRoot
            if (firstLeft == null) {
                firstLeft = bounds.left
                firstTop = bounds.top
            }
            assertEquals(firstLeft!!, bounds.left, 1f)
            assertEquals(firstTop!!, bounds.top, 1f)
            saveScreen("spacing-${tab.name.lowercase()}")
        }
    }

    @Test
    fun cardsSharePaddingAndSectionGap() {
        var pixelsPerDp = 0f
        showScreen {
            pixelsPerDp = LocalDensity.current.density
            Column(Modifier.padding(UiSpacing.page), verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
                LogCard(Modifier.testTag("first-card")) {
                    Box(Modifier.fillMaxWidth().height(40.dp).testTag("first-content"))
                }
                ClickableLogCard({}, Modifier.testTag("second-card")) {
                    Box(Modifier.fillMaxWidth().height(40.dp).testTag("second-content"))
                }
            }
        }
        val first = screen.onNodeWithTag("first-card").fetchSemanticsNode().boundsInRoot
        val second = screen.onNodeWithTag("second-card").fetchSemanticsNode().boundsInRoot
        val gap = 16 * pixelsPerDp
        val cardPadding = 12 * pixelsPerDp
        assertEquals(gap, second.top - first.bottom, 1f)
        listOf("first" to first, "second" to second).forEach { (name, card) ->
            val content = screen.onNodeWithTag("$name-content", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            assertEquals(cardPadding, content.left - card.left, 1f)
            assertEquals(cardPadding, card.right - content.right, 1f)
            assertEquals(cardPadding, content.top - card.top, 1f)
            assertEquals(cardPadding, card.bottom - content.bottom, 1f)
        }
    }

    @Test
    fun cardTitlesUseTheSameTextStyle() {
        showScreen {
            TodayScreen(sampleLogbook(), englishStrings, {}, {}, PhoneStepState(), {}, {})
        }
        listOf(englishStrings.dailyBalance, englishStrings.steps, englishStrings.dailyMacros, englishStrings.profileData).forEach { title ->
            val layout = mutableListOf<TextLayoutResult>()
            screen.onNodeWithText(title, useUnmergedTree = true).performScrollTo()
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layout) }
            assertEquals(16.sp, layout.single().layoutInput.style.fontSize)
            assertEquals(24.sp, layout.single().layoutInput.style.lineHeight)
        }
    }

    @Test
    fun profileFieldsHaveEqualGaps() {
        var pixelsPerDp = 0f
        showScreen {
            pixelsPerDp = LocalDensity.current.density
            Column(Modifier.padding(UiSpacing.page).verticalScroll(rememberScrollState())) {
                BodyMetricsEditor(BodySnapshot().toBodyDraft(), englishStrings, {}, {}, {})
            }
        }
        val age = screen.onNode(hasText(englishStrings.age) and hasSetTextAction()).fetchSemanticsNode().boundsInRoot
        val height = screen.onNode(hasText(englishStrings.height) and hasSetTextAction()).fetchSemanticsNode().boundsInRoot
        val weight = screen.onNode(hasText(englishStrings.weight) and hasSetTextAction()).fetchSemanticsNode().boundsInRoot
        val fat = screen.onNode(hasText(englishStrings.bodyFat) and hasSetTextAction()).fetchSemanticsNode().boundsInRoot
        assertEquals(12 * pixelsPerDp, height.left - age.right, 1f)
        assertEquals(12 * pixelsPerDp, fat.left - weight.right, 1f)
        assertEquals(16 * pixelsPerDp, weight.top - age.bottom, 1f)
        assertEquals(16 * pixelsPerDp, fat.top - height.bottom, 1f)
        saveScreen("spacing-profile")
    }

    @Test
    fun planEditorKeepsFieldsAndSaveReachable() {
        showScreen(width = 320.dp, fontScale = 1.5f) {
            TrainScreen(Logbook(), germanStrings, {}, emptyList(), UiLanguage.German, {}, {}, {}, {}, {}, {})
        }
        screen.onNodeWithText(germanStrings.createPlan).performScrollTo().performClick()
        screen.onNodeWithText(germanStrings.planName).performScrollTo().performTextInput("Training")
        screen.onNodeWithText(germanStrings.planExerciseName).performScrollTo().performTextInput("Bankdrücken")
        Espresso.closeSoftKeyboard()
        screen.onNode(hasText(germanStrings.addThisExercise) and hasClickAction()).performScrollTo().performClick()
        val preview = mutableListOf<TextLayoutResult>()
        screen.onNodeWithText(germanStrings.planPreview).performScrollTo()
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(preview) }
        assertEquals(1, preview.single().lineCount)
        saveScreen("spacing-plan-preview-large")
        screen.onNodeWithText(germanStrings.addPlan).performScrollTo().assertIsDisplayed()
        saveScreen("spacing-plan-large")
    }

    private fun showScreen(
        theme: UiThemeMode = UiThemeMode.Light,
        width: Dp = 412.dp,
        fontScale: Float = 1f,
        content: @Composable () -> Unit
    ) {
        screen.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                CalorieCoreTheme(theme) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        Surface(
                            modifier = Modifier.width(width).fillMaxHeight(),
                            color = MaterialTheme.colorScheme.background,
                            content = content
                        )
                    }
                }
            }
        }
    }

    private fun sampleLogbook(): Logbook {
        val now = phoneNowMillis()
        val body = (0..6).map { daysAgo ->
            BodySnapshot(
                loggedAt = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault())
                    .minusDays(daysAgo.toLong()).toInstant().toEpochMilli(),
                weightKg = 80.0 + daysAgo * 0.2,
                bodyFatPercent = 22.0 + daysAgo * 0.1,
                sleepHours = 6.5 + daysAgo * 0.15,
                steps = 6420 + daysAgo * 300,
                watchActiveCalories = 300
            )
        }
        val meals = body.map { day ->
            FoodEntry(
                loggedAt = day.loggedAt, name = "Lunch", servingGrams = 420,
                calories = 650, proteinGrams = 42.0, carbGrams = 82.0, fatGrams = 18.0
            )
        }
        val activities = body.mapIndexed { index, day ->
            ActivityEntry(loggedAt = day.loggedAt, name = "Walk", source = ActivitySource.Manual,
                activeCalories = 150 + index * 20)
        }
        val sessions = body.filterIndexed { index, _ -> index % 2 == 0 }.map { day ->
            TrainingSession(loggedAt = day.loggedAt, title = "Workout", exercises = listOf(
                LoggedExercise(name = "Row", sets = List(3) { LoggedSet(reps = 10, weightKg = 40.0) })
            ))
        }
        return Logbook(selectedDateTime = now, profile = body.first(), bodyHistory = body,
            foodEntries = meals, activityEntries = activities, trainingSessions = sessions)
    }

    private fun saveScreen(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(context.getExternalFilesDir(null), "ui-checks")
        folder.mkdirs()
        screen.waitForIdle()
        SystemClock.sleep(300)
        val bitmap = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(folder, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
