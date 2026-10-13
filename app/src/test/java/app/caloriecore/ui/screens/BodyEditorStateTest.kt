package app.caloriecore.ui.screens

import app.caloriecore.ui.model.BodySnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BodyEditorStateTest {
    @Test
    fun keepsUnfinishedNumberInput() {
        val draft = BodyDraft(BodySnapshot()).copy(weight = "82.")
        assertEquals("82.", draft.weight)
        assertEquals(82.0, draft.buildProfile()!!.weightKg, 0.0)
        assertFalse(draft.copy(weight = "").canSave)
        assertEquals(82.5, draft.copy(weight = "82.5").buildProfile()!!.weightKg, 0.0)
    }

    @Test
    fun rejectsBadBodyValues() {
        val draft = BodyDraft(BodySnapshot())
        assertFalse(draft.copy(height = "0").canSave)
        assertFalse(draft.copy(age = "12").canSave)
        assertFalse(draft.copy(weight = "0").canSave)
        assertFalse(draft.copy(bodyFat = "90").canSave)
        assertFalse(draft.copy(sleep = "25").canSave)
        assertFalse(draft.copy(watchCalories = "-1").canSave)
        assertFalse(draft.copy(pulse = "0").canSave)
        assertFalse(draft.copy(weight = "1..2").canSave)
    }

    @Test
    fun changingStepsDoesNotCreateAWeightMeasurement() {
        val draft = BodyDraft(BodySnapshot(hasWeightEntry = false)).copy(steps = "1200")
        assertFalse(draft.buildProfile()!!.hasWeightEntry)
        assertTrue(draft.copy(weightEdited = true).buildProfile()!!.hasWeightEntry)
    }
}
