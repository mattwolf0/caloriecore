package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.LoggedExercise
import app.caloriecore.ui.model.LoggedSet
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.logMomentText
import app.caloriecore.ui.model.newLogId
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber

@Composable
internal fun WorkoutLogger(
    draft: WorkoutDraft,
    strings: CalorieCoreStrings,
    onSaveSession: (TrainingSession) -> Unit,
    onClear: () -> Unit
) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShelfHeader(strings.trainingSession, if (draft.isEditing) strings.editWorkout else null)
            Text(
                text = "${strings.entryTime}: ${logMomentText(draft.workoutLoggedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.material3.OutlinedTextField(
                value = draft.workoutTitleInput,
                onValueChange = { draft.workoutTitleInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.sessionTitle) },
                singleLine = true
            )
            draft.setLogScratchpad.forEach { exercise ->
                SessionExerciseEditor(
                    exercise = exercise,
                    strings = strings,
                    onChange = draft::updateExercise,
                    onDelete = { draft.removeExercise(exercise.id) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SolidActionButton(
                    text = if (draft.isEditing) strings.updateWorkout else strings.saveWorkout,
                    enabled = draft.canSave,
                    modifier = Modifier.weight(1f),
                    onClick = { onSaveSession(draft.buildSession()) }
                )
                TextButton(onClick = onClear) { Text(strings.clear) }
            }
        }
    }
}

@Composable
private fun SessionExerciseEditor(
    exercise: LoggedExercise,
    strings: CalorieCoreStrings,
    onChange: (LoggedExercise) -> Unit,
    onDelete: () -> Unit
) {
    var reps by rememberSaveable(exercise.id) { mutableStateOf("10") }
    var weight by rememberSaveable(exercise.id) { mutableStateOf("") }

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDelete) { Text(strings.delete) }
            }
            if (exercise.sets.isEmpty()) {
                Text(strings.noSets, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                exercise.sets.forEachIndexed { index, set ->
                    SetEditorRow(
                        index = index,
                        set = set,
                        strings = strings,
                        onChange = { nextSet ->
                            onChange(
                                exercise.copy(
                                    sets = exercise.sets.map {
                                        if (it.id == set.id) nextSet else it
                                    }
                                )
                            )
                        },
                        onDuplicate = { onChange(exercise.copy(sets = exercise.sets + set.copy(id = newLogId()))) },
                        onDelete = { onChange(exercise.copy(sets = exercise.sets.filterNot { it.id == set.id })) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                NumberInput(strings.reps, reps, { reps = keepLogNumberText(it) }, Modifier.weight(1f))
                NumberInput(strings.load, weight, { weight = keepLogNumberText(it, true) }, Modifier.weight(1f), "kg", true)
                TextButton(
                    enabled = reps.isNotBlank(),
                    onClick = {
                        onChange(
                            exercise.copy(
                                sets = exercise.sets + LoggedSet(
                                    reps = reps.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                                    weightKg = weight.toDoubleOrNull() ?: 0.0
                                )
                            )
                        )
                    }
                ) { Text(strings.addSet) }
            }
        }
    }
}

@Composable
private fun SetEditorRow(
    index: Int,
    set: LoggedSet,
    strings: CalorieCoreStrings,
    onChange: (LoggedSet) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#${index + 1}", fontWeight = FontWeight.Bold)
                set.plannedReps?.let { plannedReps ->
                    MiniBadge(
                        "${strings.planned}: $plannedReps x ${CalorieCoreFormatter.kilograms(set.plannedWeightKg ?: 0.0)}",
                        color = FoodAmber
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberInput(strings.reps, set.reps.toString(), { typedReps ->
                    keepLogNumberText(typedReps).toIntOrNull()?.let { onChange(set.copy(reps = it.coerceAtLeast(1))) }
                }, Modifier.weight(1f))
                NumberInput(strings.load, CalorieCoreFormatter.logDecimal(set.weightKg), { typedLoad ->
                    keepLogNumberText(typedLoad, allowDecimal = true)
                        .toDoubleOrNull()
                        ?.let { onChange(set.copy(weightKg = it)) }
                }, Modifier.weight(1f), "kg", true)
                TextButton(onClick = onDuplicate) { Text(strings.duplicate) }
                TextButton(onClick = onDelete) { Text(strings.delete) }
            }
        }
    }
}
