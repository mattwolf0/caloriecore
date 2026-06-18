package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MiniBadge
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.components.keepLogNumberText
import app.caloriecore.ui.model.PlannedSetBlock
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingPlanExercise
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber

@Composable
internal fun PlanBuilder(
    draft: TrainingPlanDraft,
    strings: CalorieCoreStrings,
    onSavePlan: (TrainingPlan) -> Unit,
    onClose: () -> Unit
) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShelfHeader(if (draft.isEditing) strings.editPlan else strings.createPlan)
            OutlinedTextField(
                value = draft.planNameInput,
                onValueChange = { draft.planNameInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.planName) },
                singleLine = true
            )
            if (draft.planScratchpad.isNotEmpty()) {
                ShelfHeader(strings.planPreview, "${draft.planScratchpad.size} ${strings.exercises}")
                draft.planScratchpad.forEach { exercise ->
                    TrainingPlanExerciseRow(
                        exercise = exercise,
                        strings = strings,
                        onDelete = { draft.removeExercise(exercise.id) }
                    )
                }
            }
            ShelfHeader(strings.addPlanExercise)
            OutlinedTextField(
                value = draft.exerciseNameInput,
                onValueChange = { draft.exerciseNameInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.planExerciseName) },
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInput(
                    strings.sets,
                    draft.plannedSetCountInput,
                    { draft.plannedSetCountInput = keepLogNumberText(it) },
                    Modifier.weight(1f)
                )
                NumberInput(
                    strings.reps,
                    draft.plannedRepsInput,
                    { draft.plannedRepsInput = keepLogNumberText(it) },
                    Modifier.weight(1f)
                )
                NumberInput(
                    strings.load,
                    draft.plannedLoadInput,
                    { draft.plannedLoadInput = keepLogNumberText(it, true) },
                    Modifier.weight(1f),
                    "kg",
                    true
                )
            }
            SolidActionButton(
                text = strings.addThisExercise,
                enabled = draft.exerciseNameInput.isNotBlank(),
                onClick = draft::addExercise
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SolidActionButton(
                    text = if (draft.isEditing) strings.updatePlan else strings.addPlan,
                    enabled = draft.canSave,
                    modifier = Modifier.weight(1f),
                    onClick = { onSavePlan(draft.buildPlan()) }
                )
                TextButton(onClick = onClose) { Text(strings.close) }
            }
        }
    }
}

@Composable
private fun TrainingPlanExerciseRow(
    exercise: TrainingPlanExercise,
    strings: CalorieCoreStrings,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                exercise.plannedSets
                    .ifEmpty { listOf(PlannedSetBlock(setCount = 0, reps = 0, weightKg = 0.0)) }
                    .forEach { planned ->
                        MiniBadge(formatPlannedBlock(planned), color = FoodAmber)
                    }
            }
        }
        TextButton(onClick = onDelete) { Text(strings.delete) }
    }
}
