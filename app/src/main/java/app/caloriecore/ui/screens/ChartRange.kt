package app.caloriecore.ui.screens

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

internal data class ChartRange(val low: Double, val high: Double)

internal fun chartRange(values: List<Double>, startAtZero: Boolean): ChartRange {
    val validValues = values.filter { it.isFinite() }
    val min = validValues.minOrNull() ?: 0.0
    val max = validValues.maxOrNull() ?: 0.0
    if (startAtZero) {
        val low = minOf(min, 0.0)
        val high = maxOf(max, 0.0)
        val stepGuess = ((high - low) / 4).coerceAtLeast(1.0)
        val stepBase = 10.0.pow(floor(log10(stepGuess)))
        val step = listOf(1.0, 2.0, 5.0, 10.0).first { it * stepBase >= stepGuess } * stepBase
        val bottom = floor(low / (step * 2)) * step * 2
        val top = ceil(high / (step * 2)) * step * 2
        return ChartRange(bottom, if (top > bottom) top else bottom + 2)
    }
    val padding = max((max - min) * 0.15, 0.5)
    return ChartRange(floor((min - padding) * 10) / 10, ceil((max + padding) * 10) / 10)
}
