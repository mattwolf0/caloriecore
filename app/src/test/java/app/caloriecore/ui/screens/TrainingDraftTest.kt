package app.caloriecore.ui.screens

import androidx.compose.runtime.mutableStateOf
import app.caloriecore.ui.model.PlannedSetBlock
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
        assertEquals(1, draft.planScratchpad.size)
        assertEquals(4, draft.planScratchpad.first().plannedSets.first().setCount)
        assertEquals(55.5, draft.planScratchpad.first().plannedSets.first().weightKg, 0.0)
    }

    @Test
    fun ignoresBlankExerciseName() {
        val draft = trainingPlanDraftForTest()
        draft.exerciseNameInput = " "

        draft.addExercise()

        assertFalse(draft.canSave)
        assertEquals(0, draft.planScratchpad.size)
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
        assertEquals(2, draft.setLogScratchpad.first().sets.size)
        assertEquals(12, draft.setLogScratchpad.first().sets.first().plannedReps)
    }

    private fun trainingPlanDraftForTest(): TrainingPlanDraft = TrainingPlanDraft(
        openPlanIdState = mutableStateOf(null),
        planNameInputState = mutableStateOf(""),
        exerciseNameInputState = mutableStateOf(""),
        plannedSetCountInputState = mutableStateOf("3"),
        plannedRepsInputState = mutableStateOf("10"),
        plannedLoadInputState = mutableStateOf(""),
        planScratchpadState = mutableStateOf(emptyList())
    )

    private fun workoutDraftForTest(selectedDateTime: Long): WorkoutDraft = WorkoutDraft(
        openWorkoutIdState = mutableStateOf(null),
        startedPlanIdState = mutableStateOf(null),
        workoutLoggedAtState = mutableStateOf(selectedDateTime),
        workoutTitleInputState = mutableStateOf(""),
        setLogScratchpadState = mutableStateOf(emptyList())
    )
}
