package app.caloriecore.ui.format

import java.util.Locale
import kotlin.math.roundToInt

object CalorieCoreFormatter {
    fun logDecimal(amount: Double, maxDecimals: Int = 1): String {
        if (amount % 1.0 == 0.0) return amount.toInt().toString()
        // Keep numbers the same in food and gym rows.
        val pattern = when (maxDecimals) {
            0 -> "%.0f"
            2 -> "%.2f"
            else -> "%.1f"
        }
        return String.format(Locale.US, pattern, amount).trimEnd('0').trimEnd('.')
    }

    fun kcal(calories: Int): String = "$calories kcal"

    fun grams(grams: Double): String = "${logDecimal(grams)} g"

    fun kilograms(kilograms: Double): String = "${logDecimal(kilograms)} kg"

    fun kilograms(kilograms: Int): String = "$kilograms kg"

    fun bpm(restingPulse: Int): String = "$restingPulse bpm"

    fun percentFromRatio(ratio: Double): String = "${(ratio * 100).roundToInt()}%"

    fun percent(percent: Double): String = "${logDecimal(percent)}%"

    fun signedDelta(change: Double, unit: String): String {
        val prefix = if (change >= 0) "+" else ""
        return "$prefix${logDecimal(change)} $unit"
    }
}
