package app.caloriecore.ui.screens

import app.caloriecore.ui.model.BodySnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BodyDraftTest {
    @Test
    fun clearingAnInputDoesNotChangeTheSavedProfile() {
        val profile = BodySnapshot(weightKg = 80.0)
        val draft = profile.toBodyDraft().copy(weight = "")
        assertEquals("", draft.weight)
        assertFalse(draft.canSave)
        assertEquals(80.0, profile.weightKg, 0.0)
    }

    @Test
    fun savesDecimalsAndKeepsTheOriginalEntry() {
        val profile = BodySnapshot(id = 71L, loggedAt = 1234L, weightKg = 80.0)
        val draft = profile.toBodyDraft().copy(weight = "80.5", sleep = "7.25", bodyFat = "")
        assertTrue(draft.canSave)
        val saved = draft.buildProfile(profile)
        assertEquals(71L, saved.id)
        assertEquals(1234L, saved.loggedAt)
        assertEquals(80.5, saved.weightKg, 0.0)
        assertEquals(7.25, saved.sleepHours, 0.0)
        assertEquals(null, saved.bodyFatPercent)
    }

    @Test
    fun refusesInvalidProfileValues() {
        val draft = BodySnapshot().toBodyDraft()
        assertFalse(draft.copy(weight = "80..5").canSave)
        assertFalse(draft.copy(sleep = "25").canSave)
        assertFalse(draft.copy(age = "0").canSave)
        assertFalse(draft.copy(activeKcal = "-1").canSave)
    }
}
