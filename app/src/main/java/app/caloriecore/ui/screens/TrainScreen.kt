package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.LogMomentPicker
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.components.StatTile
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.finishedPlanIdsOnPhoneDay
import app.caloriecore.ui.model.gymRowsForPickedDay
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber
import app.caloriecore.ui.theme.GymGreen
import kotlin.math.roundToInt

@Composable
fun TrainScreen(
    logbook: Logbook,
    strings: CalorieCoreStrings,
    onSelectedDateTimeChange: (Long) -> Unit,
    onSavePlan: (TrainingPlan) -> Unit,
    onRemovePlan: (Long) -> Unit,
    onSaveSession: (TrainingSession) -> Unit,
    onRemoveSession: (Long) -> Unit
) {
    val gymLogsForPickedDay = logbook.gymRowsForPickedDay()
    val plansFinishedToday = finishedPlanIdsOnPhoneDay(logbook.trainingSessions, logbook.selectedDateTime)
    val pickedDayVolumeKg = gymLogsForPickedDay.sumOf { it.totalVolumeKg }.roundToInt()
    val pickedDaySetCount = gymLogsForPickedDay.sumOf { it.totalSets }
    val planDraft = rememberTrainingPlanDraft()
    val workoutDraft = rememberWorkoutDraft(logbook.selectedDateTime)

    var showPlanBuilder by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(logbook.selectedDateTime) {
        workoutDraft.followSelectedMoment(logbook.selectedDateTime)
    }

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { ScreenName(title = strings.trainTitle) }
        item {
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShelfHeader(strings.selectedMoment)
                    LogMomentPicker(
                        pickedMillis = logbook.selectedDateTime,
                        onValueChange = onSelectedDateTimeChange,
                        dateLabel = strings.date,
                        timeLabel = strings.time,
                        todayText = strings.todayButton
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(
                    strings.volume,
                    CalorieCoreFormatter.kilograms(pickedDayVolumeKg),
                    strings.todayWorkout,
                    Modifier.weight(1f),
                    GymGreen
                )
                StatTile(strings.sets, pickedDaySetCount.toString(), strings.workSets, Modifier.weight(1f), FoodAmber)
            }
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
            onDeletePlan = { plan -> onRemovePlan(plan.id) },
            onEditSession = workoutDraft::reopen,
            onDeleteSession = { session -> onRemoveSession(session.id) }
        )
        if (showPlanBuilder) {
            item {
                PlanBuilder(
                    draft = planDraft,
                    strings = strings,
                    onSavePlan = { plan ->
                        onSavePlan(plan)
                        planDraft.clear()
                        showPlanBuilder = false
                    },
                    onClose = {
                        planDraft.clear()
                        showPlanBuilder = false
                    }
                )
            }
        }
        if (workoutDraft.isOpen) {
            item {
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
        item { Spacer(modifier = Modifier.height(54.dp)) }
    }
}
