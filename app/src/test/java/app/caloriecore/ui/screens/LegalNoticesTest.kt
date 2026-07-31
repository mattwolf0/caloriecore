package app.caloriecore.ui.screens

import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.text.calorieCoreStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalNoticesTest {
    @Test
    fun keepsLegalNoticeIdsUnique() {
        val notices = legalNotices(calorieCoreStrings(UiLanguage.English))

        assertEquals(notices.size, notices.map { it.id }.distinct().size)
        assertTrue(notices.all { it.url.startsWith("https://") })
    }

    @Test
    fun includesCurrentDataAndServiceSources() {
        val notices = legalNotices(calorieCoreStrings(UiLanguage.English))
        val noticeIds = notices.map { it.id }.toSet()

        assertTrue("adult-compendium-2024" in noticeIds)
        assertTrue("open-food-facts" in noticeIds)
        assertTrue("calorie-api" in noticeIds)
        assertTrue("google-play-services" in noticeIds)
        assertEquals(LegalNoticeKind.entries.toSet(), notices.map { it.kind }.toSet())
    }
}
