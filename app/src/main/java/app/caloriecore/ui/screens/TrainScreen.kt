package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.caloriecore.ui.components.ConfirmActionDialog
import app.caloriecore.ui.components.DayPicker
import app.caloriecore.ui.components.EditorPage
import app.caloriecore.ui.components.InputRow
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MetricValue
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.ActivityCatalogItem
import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.activityRowsForPickedDay
import app.caloriecore.ui.model.finishedPlanIdsOnPhoneDay
import app.caloriecore.ui.model.gymRowsForPickedDay
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing
import kotlin.math.roundToInt

@Composable
fun TrainScreen(
    logbook: Logbook,
    strings: CalorieCoreStrings,
    onSelectedDateTimeChange: (Long) -> Unit,
    activityCatalog: List<ActivityCatalogItem>,
    activityLanguage: UiLanguage,
    onSaveActivity: (ActivityEntry) -> Unit,
    onRemoveActivity: (Long) -> Unit,
    onSavePlan: (TrainingPlan) -> Unit,
    onRemovePlan: (Long) -> Unit,
    onSaveSession: (TrainingSession) -> Unit,
    onRemoveSession: (Long) -> Unit,
    onEditorOpenChange: (Boolean) -> Unit = {}
) {
    val gymLogsForPickedDay = remember(logbook) { logbook.gymRowsForPickedDay() }
    val activitiesForPickedDay = remember(logbook) { logbook.activityRowsForPickedDay() }
    val plansFinishedToday = remember(logbook) {
        finishedPlanIdsOnPhoneDay(logbook.trainingSessions, logbook.selectedDateTime)
    }
    val pickedDayVolumeKg = remember(gymLogsForPickedDay) {
        gymLogsForPickedDay.sumOf { it.totalVolumeKg }.roundToInt()
    }
    val pickedDaySetCount = remember(gymLogsForPickedDay) {
        gymLogsForPickedDay.sumOf { it.totalSets }
    }
    val planDraft = rememberTrainingPlanDraft()
    val workoutDraft = rememberWorkoutDraft(logbook.selectedDateTime)
    val activityDraft = rememberActivityDraft(logbook.selectedDateTime, logbook.profile.weightKg)

    var showPlanBuilder by rememberSaveable { mutableStateOf(false) }
    var deletePlanId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleteSessionId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleteActivityId by rememberSaveable { mutableStateOf<Long?>(null) }

    if (deletePlanId != null || deleteSessionId != null || deleteActivityId != null) {
        ConfirmActionDialog(
            title = strings.deleteConfirmTitle,
            message = strings.deleteConfirmText,
            confirmText = strings.delete,
            cancelText = strings.cancel,
            onConfirm = {
                deletePlanId?.let(onRemovePlan)
                deleteSessionId?.let(onRemoveSession)
                deleteActivityId?.let(onRemoveActivity)
                deletePlanId = null
                deleteSessionId = null
                deleteActivityId = null
            },
            onDismiss = {
                deletePlanId = null
                deleteSessionId = null
                deleteActivityId = null
            }
        )
    }

    LaunchedEffect(logbook.selectedDateTime) {
        workoutDraft.followSelectedMoment(logbook.selectedDateTime)
        activityDraft.followSelectedMoment(logbook.selectedDateTime, logbook.profile.weightKg)
    }

    if (activityDraft.draft.isOpen) {
        val hasActivityChanges = activityDraft.draft.catalogCode != null ||
            activityDraft.draft.otherName.isNotBlank() ||
            activityDraft.draft.otherCalories.isNotBlank()
        EditorPage(
            strings = strings,
            hasChanges = hasActivityChanges,
            onClose = { activityDraft.close(logbook.selectedDateTime, logbook.profile.weightKg) },
            onEditorOpenChange = onEditorOpenChange
        ) { onCancel ->
            ActivityEditor(
                state = activityDraft,
                catalog = activityCatalog,
                language = activityLanguage,
                strings = strings,
                onSave = { entry ->
                    onSaveActivity(entry)
                    activityDraft.close(logbook.selectedDateTime, logbook.profile.weightKg)
                },
                onClose = onCancel
            )
        }
        return
    }
    if (showPlanBuilder) {
        val hasPlanChanges = planDraft.planNameInput.isNotBlank() ||
            planDraft.exerciseNameInput.isNotBlank() || planDraft.planExercises.isNotEmpty()
        EditorPage(
            strings = strings,
            hasChanges = hasPlanChanges,
            onClose = {
                planDraft.clear()
                showPlanBuilder = false
            },
            onEditorOpenChange = onEditorOpenChange
        ) { onCancel ->
            PlanBuilder(
                draft = planDraft,
                strings = strings,
                onSavePlan = { plan ->
                    onSavePlan(plan)
                    planDraft.clear()
                    showPlanBuilder = false
                },
                onClose = onCancel
            )
        }
        return
    }
    if (workoutDraft.isOpen) {
        EditorPage(
            strings = strings,
            hasChanges = true,
            onClose = { workoutDraft.close(logbook.selectedDateTime) },
            onEditorOpenChange = onEditorOpenChange
        ) { onCancel ->
            WorkoutLogger(
                draft = workoutDraft,
                strings = strings,
                onSaveSession = { session ->
                    onSaveSession(session)
                    workoutDraft.close(logbook.selectedDateTime)
                },
                onClose = onCancel
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(UiSpacing.page),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.section)
    ) {
        item { ScreenName(title = strings.trainTitle) }
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
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
                    InputRow {
                        MetricValue(
                            label = strings.volume,
                            value = CalorieCoreFormatter.kilograms(pickedDayVolumeKg),
                            modifier = Modifier.weight(1f)
                        )
                        MetricValue(
                            label = strings.sets,
                            value = pickedDaySetCount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    SolidActionButton(
                        text = strings.startWorkout,
                        onClick = {
                            workoutDraft.startNewWorkout(logbook.selectedDateTime, strings.trainingSession)
                        }
                    )
                }
            }
        }
        item {
            ActivityLogCard(
                entries = activitiesForPickedDay,
                strings = strings,
                isEditorOpen = activityDraft.draft.isOpen,
                onAdd = { activityDraft.start(logbook.selectedDateTime, logbook.profile.weightKg) },
                onEdit = { entry -> activityDraft.reopen(entry, logbook.profile.weightKg) },
                onDelete = { entry -> deleteActivityId = entry.id }
            )
        }
        trainingRows(
            sessions = gymLogsForPickedDay,
            plans = logbook.trainingPlans,
            finishedPlanIds = plansFinishedToday,
            strings = strings,
            showPlanBuilder = showPlanBuilder,
            onCreatePlan = {
                planDraft.clear()
                showPlanBuilder = true
            },
            onStartPlan = { plan -> workoutDraft.startFromPlan(plan, logbook.selectedDateTime) },
            onEditPlan = { plan ->
                planDraft.reopen(plan)
                showPlanBuilder = true
            },
            onDeletePlan = { plan -> deletePlanId = plan.id },
            onEditSession = workoutDraft::reopen,
            onDeleteSession = { session -> deleteSessionId = session.id }
        )
    }
}
