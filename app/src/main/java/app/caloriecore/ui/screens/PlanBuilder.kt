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
import app.caloriecore.ui.components.EditorActions
import app.caloriecore.ui.components.InputRow
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MiniBadge
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.components.keepLogNumberText
import app.caloriecore.ui.model.PlannedSetBlock
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingPlanExercise
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber
import app.caloriecore.ui.theme.UiSpacing

@Composable
internal fun PlanBuilder(
    draft: TrainingPlanDraft,
    strings: CalorieCoreStrings,
    onSavePlan: (TrainingPlan) -> Unit,
    onClose: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
        ScreenName(if (draft.isEditing) strings.editPlan else strings.createPlan)
        LogCard {
            OutlinedTextField(
                value = draft.planNameInput,
                onValueChange = { draft.planNameInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.planName) },
                shape = MaterialTheme.shapes.small, singleLine = true
            )
        }
        if (draft.planExercises.isNotEmpty()) {
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
                    ShelfHeader(strings.planPreview, "${draft.planExercises.size} ${strings.exercises}")
                    draft.planExercises.forEach { exercise ->
                        TrainingPlanExerciseRow(
                            exercise = exercise,
                            strings = strings,
                            onDelete = { draft.removeExercise(exercise.id) }
                        )
                    }
                }
            }
        }
        LogCard {
            Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
                ShelfHeader(strings.addPlanExercise)
                OutlinedTextField(
                    value = draft.exerciseNameInput,
                    onValueChange = { draft.exerciseNameInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(strings.planExerciseName) },
                    shape = MaterialTheme.shapes.small, singleLine = true
                )
                InputRow {
                    NumberInput(
                        label = strings.sets,
                        numberText = draft.plannedSetCountInput,
                        onValueChange = { draft.plannedSetCountInput = keepLogNumberText(it) },
                        modifier = Modifier.weight(1f)
                    )
                    NumberInput(
                        label = strings.reps,
                        numberText = draft.plannedRepsInput,
                        onValueChange = { draft.plannedRepsInput = keepLogNumberText(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                NumberInput(
                    label = strings.load,
                    numberText = draft.plannedLoadInput,
                    onValueChange = { draft.plannedLoadInput = keepLogNumberText(it, true) },
                    suffix = "kg",
                    allowDecimal = true
                )
                Text(strings.exerciseInputHint, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                SolidActionButton(
                    text = strings.addThisExercise,
                    enabled = draft.canAddExercise,
                    onClick = draft::addExercise
                )
            }
        }
        if (!draft.canSave) Text(strings.planSaveHint, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        EditorActions(
            saveText = if (draft.isEditing) strings.updatePlan else strings.addPlan,
            canSave = draft.canSave,
            strings = strings,
            onSave = { onSavePlan(draft.buildPlan()) },
            onCancel = onClose
        )
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
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(UiSpacing.tight)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
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
