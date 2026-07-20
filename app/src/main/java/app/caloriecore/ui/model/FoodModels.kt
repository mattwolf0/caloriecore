package app.caloriecore.ui.model

import kotlin.math.roundToInt

data class FoodProduct(
    val id: Long = newLogId(),
    val code: String = "",
    val name: String,
    val servingGrams: Int = 100,
    val kcalPer100g: Int? = null,
    val proteinPer100g: Double? = null,
    val carbsPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val source: String = "manual"
) {
    val hasCompleteMacros: Boolean
        get() = kcalPer100g != null && proteinPer100g != null &&
            carbsPer100g != null && fatPer100g != null

    fun toFoodEntry(loggedAt: Long, servingGrams: Int = this.servingGrams): FoodEntry {
        val eatenGrams = servingGrams.coerceAtLeast(0)
        val scale = eatenGrams / 100.0
        return FoodEntry(
            loggedAt = loggedAt,
            name = name,
            barcode = code,
            servingGrams = eatenGrams,
            calories = ((kcalPer100g ?: 0) * scale).roundToInt(),
            proteinGrams = (proteinPer100g ?: 0.0) * scale,
            carbGrams = (carbsPer100g ?: 0.0) * scale,
            fatGrams = (fatPer100g ?: 0.0) * scale
        )
    }
}

internal fun FoodProduct.withMissingMacrosFrom(fallback: FoodProduct): FoodProduct {
    if (code.isNotBlank() && fallback.code.isNotBlank() && code != fallback.code) {
        return this
    }

    return copy(
        name = if (name == "Unknown product") fallback.name else name,
        kcalPer100g = kcalPer100g ?: fallback.kcalPer100g,
        proteinPer100g = proteinPer100g ?: fallback.proteinPer100g,
        carbsPer100g = carbsPer100g ?: fallback.carbsPer100g,
        fatPer100g = fatPer100g ?: fallback.fatPer100g,
        source = "$source+${fallback.source}"
    )
}

data class FoodEntry(
    val id: Long = newLogId(),
    val loggedAt: Long = phoneNowMillis(),
    val name: String,
    val barcode: String = "",
    val servingGrams: Int,
    val calories: Int,
    val proteinGrams: Double,
    val carbGrams: Double,
    val fatGrams: Double
)

data class NutritionTotals(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

fun sumPlateMacros(foodEntries: List<FoodEntry>): NutritionTotals = NutritionTotals(
    calories = foodEntries.sumOf { it.calories },
    protein = foodEntries.sumOf { it.proteinGrams },
    carbs = foodEntries.sumOf { it.carbGrams },
    fat = foodEntries.sumOf { it.fatGrams }
)

fun mealsOnPhoneDay(foodEntries: List<FoodEntry>, selectedDateTime: Long): List<FoodEntry> = foodEntries
    .filter { samePhoneDay(it.loggedAt, selectedDateTime) }
    .sortedByDescending { it.loggedAt }
