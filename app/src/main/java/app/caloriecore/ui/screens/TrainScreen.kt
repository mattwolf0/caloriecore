package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.ConfirmActionDialog
import app.caloriecore.ui.components.DayPicker
import app.caloriecore.ui.components.FormSheet
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.PagePadding
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.StatCell
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.ActivityCatalogItem
import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.activeCaloriesTotal
import app.caloriecore.ui.model.activityRowsForPickedDay
import app.caloriecore.ui.model.finishedPlanIdsOnPhoneDay
import app.caloriecore.ui.model.gymRowsForPickedDay
import app.caloriecore.ui.model.newEntryMoment
import app.caloriecore.ui.text.CalorieCoreStrings
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
    onRemoveSession: (Long) -> Unit
) {
    val gymLogsForPickedDay = logbook.gymRowsForPickedDay()
    val activitiesForPickedDay = logbook.activityRowsForPickedDay()
    val plansFinishedToday = finishedPlanIdsOnPhoneDay(logbook.trainingSessions, logbook.selectedDateTime)
    val pickedDayVolumeKg = gymLogsForPickedDay.sumOf { it.totalVolumeKg }.roundToInt()
    val pickedDaySetCount = gymLogsForPickedDay.sumOf { it.totalSets }
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
        val closeActivity = { activityDraft.close(logbook.selectedDateTime, logbook.profile.weightKg) }
        FormSheet(
            title = if (activityDraft.draft.editingId == null) strings.addActivity else strings.updateActivity,
            closeText = strings.close,
            onDismiss = closeActivity
        ) {
            ActivityEditor(
                state = activityDraft,
                catalog = activityCatalog,
                language = activityLanguage,
                strings = strings,
                onSave = { entry ->
                    onSaveActivity(entry)
                    closeActivity()
                }
            )
        }
    }

    if (showPlanBuilder) {
        val closePlan = {
            planDraft.clear()
            showPlanBuilder = false
        }
        FormSheet(
            title = if (planDraft.isEditing) strings.editPlan else strings.createPlan,
            closeText = strings.close,
            onDismiss = closePlan
        ) {
            PlanBuilder(
                draft = planDraft,
                strings = strings,
                onSavePlan = { plan ->
                    onSavePlan(plan)
                    closePlan()
                }
            )
        }
    }

    if (workoutDraft.isOpen) {
        FormSheet(
            title = if (workoutDraft.isEditing) strings.editWorkout else strings.trainingSession,
            closeText = strings.close,
            onDismiss = { workoutDraft.close(logbook.selectedDateTime) }
        ) {
            WorkoutLogger(
                draft = workoutDraft,
                strings = strings,
                onSaveSession = { session ->
                    onSaveSession(session)
                    workoutDraft.close(logbook.selectedDateTime)
                },
                onClear = { workoutDraft.close(logbook.selectedDateTime) }
            )
        }
    }

    LazyColumn(
        contentPadding = PagePadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenName(title = strings.trainTitle) }
        item {
            DayPicker(
                pickedMillis = logbook.selectedDateTime,
                onValueChange = onSelectedDateTimeChange,
                dateLabel = strings.date,
                todayText = strings.todayButton,
                previousDayText = strings.previousDay,
                nextDayText = strings.nextDay
            )
        }
        item {
            LogCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCell(strings.volume, CalorieCoreFormatter.kilograms(pickedDayVolumeKg), MaterialTheme.colorScheme.primary)
                    StatCell(strings.sets, pickedDaySetCount.toString(), MaterialTheme.colorScheme.secondary)
                    StatCell(
                        strings.activity,
                        CalorieCoreFormatter.kcal(activitiesForPickedDay.activeCaloriesTotal()),
                        MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        activityRows(
            entries = activitiesForPickedDay,
            strings = strings,
            onAdd = { activityDraft.start(newEntryMoment(logbook.selectedDateTime), logbook.profile.weightKg) },
            onEdit = { entry -> activityDraft.reopen(entry, logbook.profile.weightKg) },
            onDelete = { entry -> deleteActivityId = entry.id }
        )
        trainingRows(
            sessions = gymLogsForPickedDay,
            plans = logbook.trainingPlans,
            finishedPlanIds = plansFinishedToday,
            strings = strings,
            onCreatePlan = {
                planDraft.clear()
                showPlanBuilder = true
            },
            onStartPlan = { plan -> workoutDraft.startFromPlan(plan, newEntryMoment(logbook.selectedDateTime)) },
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
