package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.caloriecore.ui.components.EditorActions
import app.caloriecore.ui.components.InputRow
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MiniBadge
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.ScreenName
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
import app.caloriecore.ui.theme.UiSpacing

@Composable
internal fun WorkoutLogger(
    draft: WorkoutDraft,
    strings: CalorieCoreStrings,
    onSaveSession: (TrainingSession) -> Unit,
    onClose: () -> Unit
) {
    var exerciseName by rememberSaveable { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
        ScreenName(if (draft.isEditing) strings.editWorkout else strings.trainingSession)
        LogCard {
            Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
                Text(
                    text = "${strings.entryTime}: ${logMomentText(draft.workoutLoggedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = draft.workoutTitleInput,
                    onValueChange = { draft.workoutTitleInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(strings.sessionTitle) },
                    shape = MaterialTheme.shapes.small, singleLine = true
                )
            }
        }
        draft.workoutExercises.forEach { exercise ->
            key(exercise.id) {
                SessionExerciseEditor(
                    exercise = exercise,
                    strings = strings,
                    onChange = draft::updateExercise,
                    onDelete = { draft.removeExercise(exercise.id) }
                )
            }
        }
        LogCard {
            Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(strings.planExerciseName) },
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )
                SolidActionButton(
                    text = strings.addPlanExercise,
                    onClick = {
                        draft.addExercise(exerciseName)
                        exerciseName = ""
                    },
                    enabled = exerciseName.isNotBlank()
                )
            }
        }
        if (!draft.canSave) Text(strings.workoutSaveHint, style = MaterialTheme.typography.bodySmall)
        EditorActions(
            saveText = if (draft.isEditing) strings.updateWorkout else strings.saveWorkout,
            canSave = draft.canSave,
            strings = strings,
            onSave = { onSaveSession(draft.buildSession()) },
            onCancel = onClose
        )
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
        Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiSpacing.content),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                }
                TextButton(onClick = onDelete) { Text(strings.delete) }
            }
            if (exercise.sets.isEmpty()) {
                Text(strings.noSets, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                exercise.sets.forEachIndexed { index, set ->
                    key(set.id) {
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
            }
            InputRow {
                NumberInput(
                    label = strings.reps,
                    numberText = reps,
                    onValueChange = { reps = keepLogNumberText(it) },
                    modifier = Modifier.weight(1f)
                )
                NumberInput(
                    label = strings.load,
                    numberText = weight,
                    onValueChange = { weight = keepLogNumberText(it, true) },
                    modifier = Modifier.weight(1f),
                    suffix = "kg",
                    allowDecimal = true
                )
            }
            TextButton(
                enabled = reps.toIntOrNull() in 1..1000 &&
                    (weight.isBlank() || weight.toDoubleOrNull()?.let { it in 0.0..1000.0 } == true),
                onClick = {
                    onChange(
                        exercise.copy(
                            sets = exercise.sets + LoggedSet(
                                reps = reps.toInt(),
                                weightKg = weight.toDoubleOrNull() ?: 0.0
                            )
                        )
                    )
                }
            ) { Text(strings.addSet) }
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
    var reps by rememberSaveable(set.id) { mutableStateOf(set.reps.toString()) }
    var weight by rememberSaveable(set.id) { mutableStateOf(CalorieCoreFormatter.logDecimal(set.weightKg)) }
    val validSet = reps.toIntOrNull() in 1..1000 &&
        (weight.isBlank() || weight.toDoubleOrNull()?.let { it in 0.0..1000.0 } == true)

    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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
        InputRow {
            NumberInput(
                label = strings.reps,
                numberText = reps,
                onValueChange = {
                    reps = keepLogNumberText(it)
                    onChange(set.copy(reps = reps.toIntOrNull() ?: 0))
                },
                modifier = Modifier.weight(1f),
                isError = reps.toIntOrNull() !in 1..1000
            )
            NumberInput(
                label = strings.load,
                numberText = weight,
                onValueChange = {
                    weight = keepLogNumberText(it, allowDecimal = true)
                    val load = if (weight.isBlank()) 0.0 else weight.toDoubleOrNull() ?: -1.0
                    onChange(set.copy(weightKg = load))
                },
                modifier = Modifier.weight(1f),
                suffix = "kg",
                allowDecimal = true,
                isError = weight.isNotBlank() &&
                    weight.toDoubleOrNull()?.let { it in 0.0..1000.0 } != true
            )
        }
        InputRow {
            TextButton(onClick = onDuplicate, enabled = validSet, modifier = Modifier.weight(1f)) { Text(strings.duplicate) }
            TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) { Text(strings.delete) }
        }
    }
}
