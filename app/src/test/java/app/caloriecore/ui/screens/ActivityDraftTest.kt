package app.caloriecore.ui.screens

import app.caloriecore.ui.model.ActivityCatalogItem
import app.caloriecore.ui.model.ActivitySource
import app.caloriecore.ui.model.UiLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityDraftTest {
    @Test
    fun buildsCatalogSnapshot() {
        val draft = ActivityDraft(
            isOpen = true,
            loggedAt = 10L,
            catalogCode = "12050",
            pickedName = "Running",
            met = 8.0,
            minutes = "30",
            weightKg = 80.0
        )

        val entry = draft.buildEntry()!!

        assertTrue(draft.canSave)
        assertEquals(ActivitySource.Compendium, entry.source)
        assertEquals(294, entry.activeCalories)
        assertEquals(8.0, entry.met ?: 0.0, 0.0)
        assertEquals(80.0, entry.weightKg ?: 0.0, 0.0)
    }

    @Test
    fun rejectsBadCatalogInputs() {
        val base = ActivityDraft(
            isOpen = true,
            loggedAt = 10L,
            catalogCode = "12050",
            pickedName = "Running",
            met = 8.0,
            minutes = "30",
            weightKg = 80.0
        )

        assertFalse(base.copy(minutes = "0").canSave)
        assertFalse(base.copy(minutes = "1441").canSave)
        assertFalse(base.copy(weightKg = 0.0).canSave)
        assertFalse(base.copy(met = 1.0).canSave)
    }

    @Test
    fun buildsOtherActivityWithoutMetData() {
        val draft = ActivityDraft(
            isOpen = true,
            loggedAt = 10L,
            mode = ActivityMode.Other,
            weightKg = 80.0,
            otherName = "Moving boxes",
            otherCalories = "180"
        )

        val entry = draft.buildEntry()!!

        assertEquals(ActivitySource.Manual, entry.source)
        assertEquals(180, entry.activeCalories)
        assertEquals(null, entry.met)
        assertEquals(null, entry.durationMinutes)
    }

    @Test
    fun itemUsesHungarianName() {
        val item = ActivityCatalogItem(
            code = "17190",
            categoryCode = "17",
            category = "Walking",
            nameEn = "Walking, moderate pace",
            met = 3.8,
            nameHu = "Séta"
        )

        assertEquals("Séta", item.nameFor(UiLanguage.Hungarian))
    }
}
