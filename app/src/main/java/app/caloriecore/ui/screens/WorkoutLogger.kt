package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.components.keepLogNumberText
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.LoggedExercise
import app.caloriecore.ui.model.LoggedSet
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.logMomentText
import app.caloriecore.ui.model.newLogId
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.LineIcons

@Composable
internal fun WorkoutLogger(
    draft: WorkoutDraft,
    strings: CalorieCoreStrings,
    onSaveSession: (TrainingSession) -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            singleLine = true
        )
        draft.setLogScratchpad.forEach { exercise ->
            SessionExerciseEditor(
                exercise = exercise,
                strings = strings,
                onChange = draft::updateExercise,
                onSetInputValid = draft::setInputValid,
                onDelete = { draft.removeExercise(exercise.id) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SolidActionButton(
                text = if (draft.isEditing) strings.updateWorkout else strings.saveWorkout,
                enabled = draft.canSave,
                modifier = Modifier.weight(1f),
                onClick = { onSaveSession(draft.buildSession()) }
            )
            TextButton(onClick = onClear) { Text(strings.cancel) }
        }
    }
}

@Composable
private fun SessionExerciseEditor(
    exercise: LoggedExercise,
    strings: CalorieCoreStrings,
    onChange: (LoggedExercise) -> Unit,
    onSetInputValid: (Long, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var reps by rememberSaveable(exercise.id) { mutableStateOf("10") }
    var weight by rememberSaveable(exercise.id) { mutableStateOf("") }
    val canAddSet = reps.toIntOrNull() in 1..1000 &&
        (weight.isBlank() || weight.toDoubleOrNull()?.let { it.isFinite() && it in 0.0..2000.0 } == true)

    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = exercise.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "${strings.delete}: ${exercise.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (exercise.sets.isEmpty()) {
                Text(
                    text = strings.noSets,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                exercise.sets.forEachIndexed { index, set ->
                    key(set.id) {
                        SetEditorRow(
                            index = index,
                            set = set,
                            strings = strings,
                            onInputValid = { onSetInputValid(set.id, it) },
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberInput(strings.reps, reps, { reps = keepLogNumberText(it) }, Modifier.weight(1f))
                NumberInput(strings.load, weight, { weight = keepLogNumberText(it) }, Modifier.weight(1f), "kg", true)
                FilledTonalIconButton(
                    enabled = canAddSet,
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
                ) { Icon(Icons.Rounded.Add, contentDescription = strings.addSet) }
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
    onInputValid: (Boolean) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var reps by rememberSaveable(set.id) { mutableStateOf(set.reps.toString()) }
    var weight by rememberSaveable(set.id) { mutableStateOf(CalorieCoreFormatter.logInput(set.weightKg)) }
    var menuOpen by remember { mutableStateOf(false) }

    fun updateSet() {
        val nextReps = reps.toIntOrNull()
        val nextWeight = weight.toDoubleOrNull()
        val valid = nextReps in 1..1000 && nextWeight?.let { it.isFinite() && it in 0.0..2000.0 } == true
        onInputValid(valid)
        if (valid) onChange(set.copy(reps = checkNotNull(nextReps), weightKg = checkNotNull(nextWeight)))
    }

    val plannedText = set.plannedReps?.let { plannedReps ->
        " · ${strings.planned}: $plannedReps × ${CalorieCoreFormatter.kilograms(set.plannedWeightKg ?: 0.0)}"
    }.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "#${index + 1}$plannedText",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberInput(strings.reps, reps, { typedReps ->
                reps = keepLogNumberText(typedReps)
                updateSet()
            }, Modifier.weight(1f))
            NumberInput(strings.load, weight, { typedLoad ->
                weight = keepLogNumberText(typedLoad)
                updateSet()
            }, Modifier.weight(1f), "kg", true)
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "#${index + 1}")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(strings.duplicate) },
                        leadingIcon = { Icon(LineIcons.Copy, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(strings.delete) },
                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
