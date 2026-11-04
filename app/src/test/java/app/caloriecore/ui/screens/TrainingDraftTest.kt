package app.caloriecore.ui.screens

import androidx.compose.runtime.mutableStateOf
import app.caloriecore.ui.model.PlannedSetBlock
import app.caloriecore.ui.model.LoggedSet
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingPlanExercise
import app.caloriecore.ui.model.parseLogMoment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingDraftTest {
    @Test
    fun addsExerciseToPlanDraft() {
        val draft = trainingPlanDraftForTest()
        draft.planNameInput = "Push"
        draft.exerciseNameInput = "Bench press"
        draft.plannedSetCountInput = "4"
        draft.plannedRepsInput = "8"
        draft.plannedLoadInput = "55.5"

        draft.addExercise()

        assertTrue(draft.canSave)
        assertEquals("", draft.exerciseNameInput)
        assertEquals("3", draft.plannedSetCountInput)
        assertEquals("10", draft.plannedRepsInput)
        assertEquals(1, draft.planExercises.size)
        assertEquals(4, draft.planExercises.first().plannedSets.first().setCount)
        assertEquals(55.5, draft.planExercises.first().plannedSets.first().weightKg, 0.0)
    }

    @Test
    fun ignoresBlankExerciseName() {
        val draft = trainingPlanDraftForTest()
        draft.exerciseNameInput = " "

        draft.addExercise()

        assertFalse(draft.canSave)
        assertEquals(0, draft.planExercises.size)
    }

    @Test
    fun startsWorkoutFromPlan() {
        val loggedAt = parseLogMoment("2026-06-12", "18:00")!!
        val plan = TrainingPlan(
            id = 7L,
            title = "Friday pull",
            exercises = listOf(
                TrainingPlanExercise(
                    name = "Row",
                    plannedSets = listOf(PlannedSetBlock(setCount = 2, reps = 12, weightKg = 40.0))
                )
            )
        )
        val draft = workoutDraftForTest(loggedAt)

        draft.startFromPlan(plan, loggedAt)

        assertTrue(draft.isOpen)
        assertEquals(7L, draft.startedPlanId)
        assertEquals("Friday pull", draft.workoutTitleInput)
        assertEquals(2, draft.workoutExercises.first().sets.size)
        assertEquals(12, draft.workoutExercises.first().sets.first().plannedReps)
    }

    @Test
    fun blankWorkoutStaysOpenAndNeedsASet() {
        val draft = workoutDraftForTest(100L)
        draft.startNewWorkout(100L, "Workout")
        draft.workoutTitleInput = ""
        assertTrue(draft.isOpen)
        assertFalse(draft.canSave)
        draft.workoutTitleInput = "Push"
        draft.addExercise("Push up")
        assertFalse(draft.canSave)
        val exercise = draft.workoutExercises.first()
        draft.updateExercise(exercise.copy(sets = listOf(LoggedSet(reps = 10, weightKg = 0.0))))
        assertTrue(draft.canSave)
        assertEquals(null, draft.buildSession().programId)
        assertEquals(1, draft.buildSession().totalSets)
    }

    @Test
    fun invalidSetNumbersCannotBeAddedToAPlan() {
        val draft = trainingPlanDraftForTest()
        draft.exerciseNameInput = "Bench"
        draft.plannedSetCountInput = "0"
        assertFalse(draft.canAddExercise)
        draft.addExercise()
        assertTrue(draft.planExercises.isEmpty())
    }

    private fun trainingPlanDraftForTest(): TrainingPlanDraft = TrainingPlanDraft(
        openPlanIdState = mutableStateOf(null),
        planNameInputState = mutableStateOf(""),
        exerciseNameInputState = mutableStateOf(""),
        plannedSetCountInputState = mutableStateOf("3"),
        plannedRepsInputState = mutableStateOf("10"),
        plannedLoadInputState = mutableStateOf(""),
        planExercisesState = mutableStateOf(emptyList())
    )

    private fun workoutDraftForTest(selectedDateTime: Long): WorkoutDraft = WorkoutDraft(
        openWorkoutIdState = mutableStateOf(null),
        startedPlanIdState = mutableStateOf(null),
        workoutLoggedAtState = mutableStateOf(selectedDateTime),
        workoutTitleInputState = mutableStateOf(""),
        workoutExercisesState = mutableStateOf(emptyList())
    )
}
