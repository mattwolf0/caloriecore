package app.caloriecore.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressStatsTest {
    @Test
    fun averagesIgnoreMissingDaysButKeepZero() {
        val stats = ProgressStats(listOf(
            emptyDay(),
            emptyDay().copy(intakeKcal = 0, balanceKcal = -1600, steps = 0),
            emptyDay().copy(intakeKcal = 1200, balanceKcal = -400, steps = 6000)
        ))
        assertEquals(600.0, stats.averageIntake!!, 0.0)
        assertEquals(-1000.0, stats.averageBalance!!, 0.0)
        assertEquals(3000.0, stats.averageSteps!!, 0.0)
        assertEquals(2, stats.foodDays)
        assertEquals(2, stats.stepDays)
    }

    @Test
    fun emptyPeriodDoesNotInventAverages() {
        val stats = ProgressStats(List(30) { emptyDay() })
        assertEquals(null, stats.averageIntake)
        assertEquals(null, stats.averageBalance)
        assertEquals(null, stats.averageSleep)
        assertEquals(null, stats.averageSteps)
        assertEquals(null, stats.weightChange)
        assertEquals(0, stats.foodDays)
        assertEquals(0, stats.bodyDays)
        assertEquals(0, stats.workouts)
    }

    @Test
    fun weightChangeUsesFirstAndLastSavedDays() {
        val stats = ProgressStats(listOf(
            emptyDay().copy(weightKg = 81.0, sleepHours = 6.0),
            emptyDay(),
            emptyDay().copy(weightKg = 80.5, sleepHours = 8.0)
        ))
        assertEquals(-0.5, stats.weightChange!!, 0.0)
        assertEquals(7.0, stats.averageSleep!!, 0.0)
        assertEquals(2, stats.bodyDays)
        assertEquals(null, ProgressStats(listOf(emptyDay().copy(weightKg = 81.0))).weightChange)
    }

    @Test
    fun activityAndWorkoutTotalsStaySeparate() {
        val stats = ProgressStats(listOf(
            emptyDay().copy(activityKcal = 150, workoutCount = 2, setCount = 12, trainingVolumeKg = 3200.0),
            emptyDay().copy(activityKcal = 200, workoutCount = 1, setCount = 8),
            emptyDay()
        ))
        assertEquals(350, stats.activityCalories)
        assertEquals(3, stats.workouts)
        assertEquals(20, stats.sets)
    }

    private fun emptyDay() = ProgressDay(
        millis = 0, label = "", weightKg = null, intakeKcal = null, burnKcal = null,
        balanceKcal = null, trainingVolumeKg = null, steps = null
    )
}
