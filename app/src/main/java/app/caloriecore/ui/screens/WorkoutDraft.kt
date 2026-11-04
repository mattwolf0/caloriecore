package app.caloriecore.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.caloriecore.ui.model.LoggedExercise
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.newLogId
import app.caloriecore.ui.model.startSheetFor

internal class WorkoutDraft internal constructor(
    private val openWorkoutIdState: MutableState<Long?>,
    private val startedPlanIdState: MutableState<Long?>,
    private val workoutLoggedAtState: MutableState<Long>,
    private val workoutTitleInputState: MutableState<String>,
    private val workoutExercisesState: MutableState<List<LoggedExercise>>,
    private val isNewWorkoutState: MutableState<Boolean> = mutableStateOf(false)
) {
    var openWorkoutId by openWorkoutIdState
    var startedPlanId by startedPlanIdState
    var workoutLoggedAt by workoutLoggedAtState
    var workoutTitleInput by workoutTitleInputState
    var workoutExercises by workoutExercisesState
    private var isNewWorkout by isNewWorkoutState

    val isOpen: Boolean
        get() = isNewWorkout || openWorkoutId != null || startedPlanId != null || workoutExercises.isNotEmpty()

    val isEditing: Boolean
        get() = openWorkoutId != null

    val canSave: Boolean
        get() = workoutTitleInput.isNotBlank() && workoutExercises.any { it.sets.isNotEmpty() } &&
            workoutExercises.all { exercise ->
                exercise.sets.all { it.reps in 1..1000 && it.weightKg in 0.0..1000.0 }
            }

    fun followSelectedMoment(selectedDateTime: Long) {
        if (openWorkoutId == null) workoutLoggedAt = selectedDateTime
    }

    fun close(selectedDateTime: Long) {
        isNewWorkout = false
        openWorkoutId = null
        startedPlanId = null
        workoutLoggedAt = selectedDateTime
        workoutTitleInput = ""
        workoutExercises = emptyList()
    }

    fun startNewWorkout(selectedDateTime: Long, title: String) {
        close(selectedDateTime)
        isNewWorkout = true
        workoutTitleInput = title
    }

    fun addExercise(name: String) {
        if (name.isBlank()) return
        workoutExercises = workoutExercises + LoggedExercise(name = name.trim(), position = workoutExercises.size)
    }

    fun startFromPlan(plan: TrainingPlan, selectedDateTime: Long) {
        // Keep the copied plan values on the workout.
        val startSheet = plan.startSheetFor(selectedDateTime)
        openWorkoutId = null
        startedPlanId = plan.id
        workoutLoggedAt = startSheet.loggedAt
        workoutTitleInput = startSheet.title
        workoutExercises = startSheet.exercises
    }

    fun reopen(session: TrainingSession) {
        openWorkoutId = session.id
        startedPlanId = session.programId
        workoutLoggedAt = session.loggedAt
        workoutTitleInput = session.title
        workoutExercises = session.exercises
    }

    fun updateExercise(next: LoggedExercise) {
        workoutExercises = workoutExercises.map { if (it.id == next.id) next else it }
    }

    fun removeExercise(exerciseId: Long) {
        workoutExercises = workoutExercises.filterNot { it.id == exerciseId }
    }

    fun buildSession(): TrainingSession {
        val sessionId = openWorkoutId ?: newLogId()
        return TrainingSession(
            id = sessionId,
            programId = startedPlanId,
            title = workoutTitleInput.trim(),
            loggedAt = workoutLoggedAt,
            exercises = workoutExercises.filter { it.sets.isNotEmpty() }.mapIndexed { index, exercise ->
                exercise.copy(
                    sessionId = sessionId,
                    position = index,
                    sets = exercise.sets.map { set -> set.copy(loggedAt = workoutLoggedAt) }
                )
            }
        )
    }
}

@Composable
internal fun rememberWorkoutDraft(selectedDateTime: Long): WorkoutDraft {
    val openWorkoutId = rememberSaveable { mutableStateOf<Long?>(null) }
    val startedPlanId = rememberSaveable { mutableStateOf<Long?>(null) }
    val workoutLoggedAt = rememberSaveable { mutableStateOf(selectedDateTime) }
    val workoutTitleInput = rememberSaveable { mutableStateOf("") }
    val workoutExercises = remember { mutableStateOf(emptyList<LoggedExercise>()) }
    val isNewWorkout = rememberSaveable { mutableStateOf(false) }

    return remember {
        WorkoutDraft(
            openWorkoutId,
            startedPlanId,
            workoutLoggedAt,
            workoutTitleInput,
            workoutExercises,
            isNewWorkout
        )
    }
}
