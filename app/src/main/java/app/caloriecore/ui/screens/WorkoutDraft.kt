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
import app.caloriecore.ui.model.canBeSaved
import app.caloriecore.ui.model.newLogId
import app.caloriecore.ui.model.startSheetFor

internal class WorkoutDraft internal constructor(
    private val openWorkoutIdState: MutableState<Long?>,
    private val startedPlanIdState: MutableState<Long?>,
    private val workoutLoggedAtState: MutableState<Long>,
    private val workoutTitleInputState: MutableState<String>,
    private val setLogScratchpadState: MutableState<List<LoggedExercise>>,
    private val invalidSetIdsState: MutableState<List<Long>> = mutableStateOf(emptyList())
) {
    var openWorkoutId by openWorkoutIdState
    var startedPlanId by startedPlanIdState
    var workoutLoggedAt by workoutLoggedAtState
    var workoutTitleInput by workoutTitleInputState
    var setLogScratchpad by setLogScratchpadState
    private var invalidSetIds by invalidSetIdsState

    val isOpen: Boolean
        get() = openWorkoutId != null || startedPlanId != null || setLogScratchpad.isNotEmpty()

    val isEditing: Boolean
        get() = openWorkoutId != null

    val canSave: Boolean
        get() = workoutTitleInput.isNotBlank() && setLogScratchpad.isNotEmpty() &&
            setLogScratchpad.any { it.sets.isNotEmpty() } && setLogScratchpad.all { exercise ->
                exercise.name.isNotBlank() && exercise.sets.all { it.canBeSaved() && it.id !in invalidSetIds }
            }

    fun followSelectedMoment(selectedDateTime: Long) {
        if (!isOpen) workoutLoggedAt = selectedDateTime
    }

    fun close(selectedDateTime: Long) {
        openWorkoutId = null
        startedPlanId = null
        workoutLoggedAt = selectedDateTime
        workoutTitleInput = ""
        setLogScratchpad = emptyList()
        invalidSetIds = emptyList()
    }

    fun startFromPlan(plan: TrainingPlan, selectedDateTime: Long) {
        val startSheet = plan.startSheetFor(selectedDateTime)
        openWorkoutId = null
        startedPlanId = plan.id
        workoutLoggedAt = startSheet.loggedAt
        workoutTitleInput = startSheet.title
        setLogScratchpad = startSheet.exercises
        invalidSetIds = emptyList()
    }

    fun reopen(session: TrainingSession) {
        openWorkoutId = session.id
        startedPlanId = session.programId
        workoutLoggedAt = session.loggedAt
        workoutTitleInput = session.title
        setLogScratchpad = session.exercises
        invalidSetIds = emptyList()
    }

    fun updateExercise(next: LoggedExercise) {
        setLogScratchpad = setLogScratchpad.map { if (it.id == next.id) next else it }
    }

    fun removeExercise(exerciseId: Long) {
        setLogScratchpad = setLogScratchpad.filterNot { it.id == exerciseId }
    }

    fun setInputValid(setId: Long, valid: Boolean) {
        invalidSetIds = if (valid) invalidSetIds - setId else (invalidSetIds + setId).distinct()
    }

    fun buildSession(): TrainingSession {
        val sessionId = openWorkoutId ?: newLogId()
        return TrainingSession(
            id = sessionId,
            programId = startedPlanId,
            title = workoutTitleInput.trim(),
            loggedAt = workoutLoggedAt,
            exercises = setLogScratchpad.mapIndexed { index, exercise ->
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
    val setLogScratchpad = rememberSaveable { mutableStateOf(emptyList<LoggedExercise>()) }
    val invalidSetIds = rememberSaveable { mutableStateOf(emptyList<Long>()) }

    return remember {
        WorkoutDraft(
            openWorkoutId,
            startedPlanId,
            workoutLoggedAt,
            workoutTitleInput,
            setLogScratchpad,
            invalidSetIds
        )
    }
}
