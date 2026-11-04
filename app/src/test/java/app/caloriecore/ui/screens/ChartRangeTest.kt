package app.caloriecore.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartRangeTest {
    @Test
    fun keepsFlatWeightAwayFromChartEdges() {
        val range = chartRange(listOf(80.0, 80.0), startAtZero = false)
        assertTrue(range.low < 80.0)
        assertTrue(range.high > 80.0)
    }

    @Test
    fun includesZeroForPositiveAndNegativeBars() {
        val positive = chartRange(listOf(200.0, 100.0), startAtZero = true)
        val negative = chartRange(listOf(-800.0, -50.0), startAtZero = true)
        assertEquals(0.0, positive.low, 0.0)
        assertTrue(negative.high >= 0.0)
        assertEquals(-800.0, negative.low, 0.0)
    }

    @Test
    fun handlesZeroAndMissingValuesWithoutAnEmptyRange() {
        val range = chartRange(listOf(0.0, Double.NaN), startAtZero = true)
        assertEquals(0.0, range.low, 0.0)
        assertTrue(range.high > range.low)
        assertTrue(chartRange(emptyList(), false).high.isFinite())
    }

    @Test
    fun zeroStepsHaveWholeNumberTicks() {
        val range = chartRange(listOf(0.0), startAtZero = true)
        val middle = (range.low + range.high) / 2
        assertEquals(middle.toInt().toDouble(), middle, 0.0)
    }

    @Test
    fun negativeBalanceStopsAtZero() {
        val range = chartRange(listOf(-1622.0), startAtZero = true)
        assertEquals(0.0, range.high, 0.0)
        assertTrue(range.low <= -1622)
        assertEquals(-1000.0, (range.low + range.high) / 2, 0.0)
    }
}
