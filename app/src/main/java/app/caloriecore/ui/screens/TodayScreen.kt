package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.caloriecore.ui.components.DayPicker
import app.caloriecore.ui.components.EditorPage
import app.caloriecore.ui.components.InputRow
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.SecondaryActionButton
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.PhoneStepState
import app.caloriecore.ui.model.macroTargetFor
import app.caloriecore.ui.model.phoneNowMillis
import app.caloriecore.ui.model.pickedDayReport
import app.caloriecore.ui.model.samePhoneDay
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing

@Composable
fun TodayScreen(
    logbook: Logbook,
    strings: CalorieCoreStrings,
    onSelectedDateTimeChange: (Long) -> Unit,
    onProfileChange: (BodySnapshot) -> Unit,
    phoneStepState: PhoneStepState,
    onOpenFood: () -> Unit,
    onOpenTraining: () -> Unit,
    onEditorOpenChange: (Boolean) -> Unit = {}
) {
    val report = remember(logbook) { logbook.pickedDayReport() }
    val target = remember(report.profile, report.activities, logbook.settings.nutritionGoal) {
        macroTargetFor(report.profile, report.activities, logbook.settings.nutritionGoal)
    }
    var showBurnDetails by rememberSaveable { mutableStateOf(false) }
    var editProfile by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()

    if (editProfile) {
        val original = rememberSaveable(saver = BodyDraftSaver) { logbook.profile.toBodyDraft() }
        var draft by rememberSaveable(stateSaver = BodyDraftSaver) { mutableStateOf(original) }
        EditorPage(
            strings = strings,
            hasChanges = draft != original,
            onClose = { editProfile = false },
            onEditorOpenChange = onEditorOpenChange
        ) { onCancel ->
            BodyMetricsEditor(
                draft = draft,
                strings = strings,
                onChange = { draft = it },
                onSave = {
                    val saved = draft.buildProfile(logbook.profile)
                    onProfileChange(
                        saved.copy(
                            steps = if (draft.steps == original.steps) logbook.profile.steps else saved.steps
                        )
                    )
                    editProfile = false
                },
                onCancel = onCancel
            )
        }
        return
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(UiSpacing.page),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.section)
    ) {
        item { ScreenName(strings.todayTitle) }
        item {
            LogCard {
                DayPicker(
                    pickedMillis = logbook.selectedDateTime,
                    onValueChange = onSelectedDateTimeChange,
                    dateLabel = strings.date,
                    todayText = strings.todayButton,
                    previousDayText = strings.previousDay,
                    nextDayText = strings.nextDay
                )
            }
        }
        item {
            DailyBurnCard(
                burn = report.burnEstimate,
                intakeCalories = report.nutrition.calories,
                strings = strings,
                hasFood = report.foodEntries.isNotEmpty(),
                showDetails = showBurnDetails,
                onToggleDetails = { showBurnDetails = !showBurnDetails }
            )
        }
        if (showBurnDetails) {
            item { BurnBreakdownCard(report.burnEstimate, strings) }
        }
        item {
            InputRow {
                SolidActionButton(
                    text = strings.addMeal,
                    onClick = onOpenFood,
                    modifier = Modifier.weight(1f)
                )
                SecondaryActionButton(
                    text = strings.train,
                    onClick = onOpenTraining,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (samePhoneDay(logbook.selectedDateTime, phoneNowMillis())) {
            item { StepCounterCard(phoneStepState, strings) }
        }
        item {
            FoodMacroSummary(
                nutrition = report.nutrition,
                target = target,
                hasMissingMacros = report.foodEntries.any { !it.proteinKnown || !it.carbsKnown || !it.fatKnown },
                strings = strings
            )
        }
        item {
            ProfileSummary(
                profile = logbook.profile,
                strings = strings,
                onEdit = { editProfile = true }
            )
        }
    }
}
