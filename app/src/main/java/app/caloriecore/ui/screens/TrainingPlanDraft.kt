package app.caloriecore.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.PlannedSetBlock
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingPlanExercise
import app.caloriecore.ui.model.newLogId

internal class TrainingPlanDraft internal constructor(
    private val openPlanIdState: MutableState<Long?>,
    private val planNameInputState: MutableState<String>,
    private val exerciseNameInputState: MutableState<String>,
    private val plannedSetCountInputState: MutableState<String>,
    private val plannedRepsInputState: MutableState<String>,
    private val plannedLoadInputState: MutableState<String>,
    private val planExercisesState: MutableState<List<TrainingPlanExercise>>
) {
    var openPlanId by openPlanIdState
    var planNameInput by planNameInputState
    var exerciseNameInput by exerciseNameInputState
    var plannedSetCountInput by plannedSetCountInputState
    var plannedRepsInput by plannedRepsInputState
    var plannedLoadInput by plannedLoadInputState
    var planExercises by planExercisesState

    val isEditing: Boolean
        get() = openPlanId != null

    val canSave: Boolean
        get() = planNameInput.isNotBlank() && planExercises.isNotEmpty()

    val canAddExercise: Boolean
        get() = exerciseNameInput.isNotBlank() && plannedSetCountInput.toIntOrNull() in 1..100 &&
            plannedRepsInput.toIntOrNull() in 1..1000 &&
            (plannedLoadInput.isBlank() || plannedLoadInput.toDoubleOrNull()?.let { it in 0.0..1000.0 } == true)

    fun clear() {
        openPlanId = null
        planNameInput = ""
        exerciseNameInput = ""
        plannedSetCountInput = "3"
        plannedRepsInput = "10"
        plannedLoadInput = ""
        planExercises = emptyList()
    }

    fun reopen(plan: TrainingPlan) {
        openPlanId = plan.id
        planNameInput = plan.title
        planExercises = plan.exercises
        exerciseNameInput = ""
        plannedSetCountInput = "3"
        plannedRepsInput = "10"
        plannedLoadInput = ""
    }

    fun addExercise() {
        if (!canAddExercise) return
        val exerciseName = exerciseNameInput.trim()
        if (exerciseName.isBlank()) return
        val plannedSet = PlannedSetBlock(
            setCount = plannedSetCountInput.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            reps = plannedRepsInput.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            weightKg = plannedLoadInput.toDoubleOrNull() ?: 0.0
        )
        planExercises = planExercises + TrainingPlanExercise(
            name = exerciseName,
            target = "",
            position = planExercises.size,
            plannedSets = listOf(plannedSet)
        )
        exerciseNameInput = ""
        plannedSetCountInput = "3"
        plannedRepsInput = "10"
        plannedLoadInput = ""
    }

    fun removeExercise(exerciseId: Long) {
        planExercises = planExercises.filterNot { it.id == exerciseId }
    }

    fun buildPlan(): TrainingPlan = TrainingPlan(
        id = openPlanId ?: newLogId(),
        title = planNameInput.trim(),
        focus = "",
        daysPerWeek = 3,
        exercises = planExercises.mapIndexed { index, exercise -> exercise.copy(position = index) }
    )
}

@Composable
internal fun rememberTrainingPlanDraft(): TrainingPlanDraft {
    val openPlanId = rememberSaveable { mutableStateOf<Long?>(null) }
    val planNameInput = rememberSaveable { mutableStateOf("") }
    val exerciseNameInput = rememberSaveable { mutableStateOf("") }
    val plannedSetCountInput = rememberSaveable { mutableStateOf("3") }
    val plannedRepsInput = rememberSaveable { mutableStateOf("10") }
    val plannedLoadInput = rememberSaveable { mutableStateOf("") }
    val planExercises = remember { mutableStateOf(emptyList<TrainingPlanExercise>()) }

    return remember {
        TrainingPlanDraft(
            openPlanId,
            planNameInput,
            exerciseNameInput,
            plannedSetCountInput,
            plannedRepsInput,
            plannedLoadInput,
            planExercises
        )
    }
}

internal fun formatPlannedBlock(planned: PlannedSetBlock): String =
    if (planned.setCount <= 0) "-" else "${planned.setCount} x ${planned.reps} @ ${CalorieCoreFormatter.kilograms(planned.weightKg)}"
