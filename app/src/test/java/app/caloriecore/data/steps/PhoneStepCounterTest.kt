package app.caloriecore.data.steps

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneStepCounterTest {
    @Test
    fun addsStepValuesFromTheDay() {
        assertEquals(4_250, totalStepCount(listOf(1_500, 2_000, 750)))
    }

    @Test
    fun ignoresInvalidNegativeValues() {
        assertEquals(800, totalStepCount(listOf(500, -100, 300)))
    }
}
