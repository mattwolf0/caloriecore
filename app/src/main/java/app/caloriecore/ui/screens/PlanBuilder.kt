package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.SoftActionButton
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.components.keepLogNumberText
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingPlanExercise
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
internal fun PlanBuilder(draft: TrainingPlanDraft, strings: CalorieCoreStrings, onSavePlan: (TrainingPlan) -> Unit) {
    OutlinedTextField(
        value = draft.planNameInput,
        onValueChange = { draft.planNameInput = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(strings.planName) },
        singleLine = true
    )
    if (draft.planScratchpad.isNotEmpty()) {
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "${strings.planPreview} · ${draft.planScratchpad.size} ${strings.exercises.lowercase()}",
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                draft.planScratchpad.forEachIndexed { index, exercise ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    TrainingPlanExerciseRow(
                        exercise = exercise,
                        strings = strings,
                        onDelete = { draft.removeExercise(exercise.id) }
                    )
                }
            }
        }
    }
    Text(
        text = strings.addPlanExercise,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
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
            { draft.plannedLoadInput = keepLogNumberText(it) },
            Modifier.weight(1f),
            "kg",
            true
        )
    }
    SoftActionButton(
        text = strings.addThisExercise,
        icon = Icons.Rounded.Add,
        enabled = draft.canAddExercise,
        onClick = draft::addExercise
    )
    SolidActionButton(
        text = if (draft.isEditing) strings.updatePlan else strings.addPlan,
        enabled = draft.canSave,
        onClick = { onSavePlan(draft.buildPlan()) }
    )
}

@Composable
private fun TrainingPlanExerciseRow(exercise: TrainingPlanExercise, strings: CalorieCoreStrings, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = exercise.plannedSets.joinToString(", ") { formatPlannedBlock(it) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Rounded.Delete,
                contentDescription = "${strings.delete}: ${exercise.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
