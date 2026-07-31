package app.caloriecore

import app.caloriecore.ui.model.ActivityCatalogItem
import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.ActivitySource
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.activeCaloriesFor
import app.caloriecore.ui.model.activitiesOnPhoneDay
import app.caloriecore.ui.model.canBeSaved
import app.caloriecore.ui.model.parseLogMoment
import app.caloriecore.ui.model.searchActivities
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityModelsTest {
    @Test
    fun calculatesActiveCalories() {
        assertEquals(294, activeCaloriesFor(met = 8.0, weightKg = 80.0, minutes = 30))
        assertEquals(0, activeCaloriesFor(met = 1.0, weightKg = 80.0, minutes = 30))
        assertEquals(0, activeCaloriesFor(met = 0.8, weightKg = 80.0, minutes = 30))
    }

    @Test
    fun rejectsInvalidCalculatedActivity() {
        assertEquals(0, activeCaloriesFor(met = 8.0, weightKg = 0.0, minutes = 30))
        assertEquals(0, activeCaloriesFor(met = 8.0, weightKg = 80.0, minutes = 0))
        assertEquals(0, activeCaloriesFor(met = 8.0, weightKg = 80.0, minutes = 1441))
        assertFalse(
            ActivityEntry(
                name = "Running",
                source = ActivitySource.Compendium,
                durationMinutes = 30,
                met = 8.0,
                weightKg = 0.0,
                activeCalories = 294
            ).canBeSaved()
        )
    }

    @Test
    fun keepsManualActivitySimple() {
        val entry = ActivityEntry(
            name = "Moving boxes",
            source = ActivitySource.Manual,
            activeCalories = 180
        )

        assertTrue(entry.canBeSaved())
    }

    @Test
    fun filtersActivityByDay() {
        val firstDay = parseLogMoment("2026-06-08", "10:00")!!
        val secondDay = parseLogMoment("2026-06-09", "10:00")!!
        val entries = listOf(
            ActivityEntry(loggedAt = firstDay, name = "Walking", source = ActivitySource.Manual, activeCalories = 100),
            ActivityEntry(loggedAt = secondDay, name = "Cycling", source = ActivitySource.Manual, activeCalories = 200)
        )

        val filtered = activitiesOnPhoneDay(entries, firstDay)

        assertEquals(listOf("Walking"), filtered.map { it.name })
    }

    @Test
    fun keepsSavedCalculationValues() {
        val entry = ActivityEntry(
            name = "Running",
            source = ActivitySource.Compendium,
            catalogCode = "12050",
            durationMinutes = 30,
            met = 8.0,
            weightKg = 80.0,
            activeCalories = activeCaloriesFor(8.0, 80.0, 30)
        )

        val currentWeight = 95.0

        assertEquals(80.0, entry.weightKg ?: 0.0, 0.0)
        assertEquals(294, entry.activeCalories)
        assertEquals(349, activeCaloriesFor(8.0, currentWeight, 30))
    }

    @Test
    fun searchesTranslatedNamesAndUsesEnglishFallback() {
        val items = listOf(
            ActivityCatalogItem(
                code = "1",
                categoryCode = "17",
                category = "Walking",
                nameEn = "Walking, moderate pace",
                met = 3.8,
                common = true,
                shortNameEn = "Walking",
                nameHu = "Séta",
                nameDe = "Gehen"
            ),
            ActivityCatalogItem(
                code = "2",
                categoryCode = "15",
                category = "Sports",
                nameEn = "Archery, non-hunting",
                met = 4.3
            )
        )

        assertEquals("1", searchActivities(items, "seta", UiLanguage.Hungarian).single().code)
        assertEquals("1", searchActivities(items, "gehen", UiLanguage.German).single().code)
        assertEquals("Archery, non-hunting", items[1].nameFor(UiLanguage.Hungarian))
        assertEquals(listOf("1"), searchActivities(items, "", UiLanguage.English).map { it.code })
    }

    @Test
    fun limitsSearchResults() {
        val items = (1..40).map { index ->
            ActivityCatalogItem(
                code = index.toString(),
                categoryCode = "17",
                category = "Walking",
                nameEn = "Walking option $index",
                met = 3.0
            )
        }

        assertEquals(30, searchActivities(items, "walking", UiLanguage.English).size)
    }
}
