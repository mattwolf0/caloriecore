package app.caloriecore

import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.nearestBodyCheckIn
import app.caloriecore.ui.model.parseLogMoment
import app.caloriecore.ui.model.samePhoneDay
import app.caloriecore.ui.model.withBodyCheckIn
import app.caloriecore.ui.model.withPickedMoment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogbookDaysTest {
    private val today = parseLogMoment("2026-10-13", "12:00")!!

    @Test
    fun newDayKeepsBodyDataAndClearsDailyValues() {
        val yesterday = parseLogMoment("2026-10-12", "12:00")!!
        val body = BodySnapshot(
            loggedAt = yesterday,
            weightKg = 75.0,
            sleepHours = 6.0,
            watchActiveCalories = 700,
            plannedWorkoutCalories = 200,
            steps = 8000
        )
        val next = nearestBodyCheckIn(listOf(body), today)

        assertEquals(75.0, next.weightKg, 0.0)
        assertEquals(0, next.watchActiveCalories)
        assertEquals(0, next.plannedWorkoutCalories)
        assertEquals(0, next.steps)
        assertEquals(0.0, next.sleepHours, 0.0)
        assertFalse(next.hasWeightEntry)
        assertNotEquals(body.id, next.id)
    }

    @Test
    fun midnightEditKeepsYesterdaysRecord() {
        val before = parseLogMoment("2026-10-12", "23:59")!! + 50_000L
        val after = parseLogMoment("2026-10-13", "00:00")!! + 10_000L
        val body = BodySnapshot(id = 123L, loggedAt = before)
        val book = Logbook(selectedDateTime = before, profile = body, bodyHistory = listOf(body))
            .withPickedMoment(after)
        val saved = book.withBodyCheckIn(book.profile.copy(steps = 12))

        assertNotEquals(body.id, book.profile.id)
        assertEquals(2, saved.bodyHistory.size)
        assertEquals(body, saved.bodyHistory.single { samePhoneDay(it.loggedAt, before) })
        assertEquals(12, saved.bodyHistory.single { samePhoneDay(it.loggedAt, after) }.steps)
    }

    @Test
    fun savingAnOldIdOnAnotherDayDoesNotMoveTheOldRecord() {
        val yesterday = parseLogMoment("2026-10-12", "12:00")!!
        val body = BodySnapshot(id = 123L, loggedAt = yesterday)
        val book = Logbook(selectedDateTime = today, bodyHistory = listOf(body))
        val saved = book.withBodyCheckIn(body.copy(steps = 12))

        assertEquals(2, saved.bodyHistory.size)
        assertTrue(body in saved.bodyHistory)
    }

    @Test
    fun olderDaysUseTheFirstProfileInsteadOfTheLatestOne() {
        val first = BodySnapshot(loggedAt = today, weightKg = 90.0)
        val later = BodySnapshot(loggedAt = parseLogMoment("2026-10-20", "12:00")!!, weightKg = 80.0)
        val oldDay = parseLogMoment("2026-10-01", "12:00")!!

        val profile = nearestBodyCheckIn(listOf(later, first), oldDay)
        assertEquals(90.0, profile.weightKg, 0.0)
        assertFalse(profile.hasWeightEntry)
    }

    @Test
    fun selectingADayUsesItsLastSavedBodyData() {
        val morning = BodySnapshot(loggedAt = today, weightKg = 82.0)
        val evening = morning.copy(id = 100L, loggedAt = today + 4 * 3_600_000L, weightKg = 80.0)
        assertEquals(evening, nearestBodyCheckIn(listOf(morning, evening), today))
        val book = Logbook(selectedDateTime = today, profile = evening, bodyHistory = listOf(morning, evening))
        val saved = book.withBodyCheckIn(evening.copy(steps = 1200))
        assertEquals(evening.loggedAt, saved.profile.loggedAt)
        assertEquals(2, saved.bodyHistory.size)
    }
}
