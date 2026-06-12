package app.caloriecore.ui.model

import kotlin.math.roundToInt

data class FoodProduct(
    val id: Long = newLogId(),
    val code: String = "",
    val name: String,
    val servingGrams: Int = 100,
    val kcalPer100g: Int = 0,
    val proteinPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val source: String = "manual"
) {
    fun toFoodEntry(loggedAt: Long, servingGrams: Int = this.servingGrams): FoodEntry {
        val eatenGrams = servingGrams.coerceAtLeast(0)
        val scale = eatenGrams / 100.0
        return FoodEntry(
            loggedAt = loggedAt,
            name = name,
            barcode = code,
            servingGrams = eatenGrams,
            calories = (kcalPer100g * scale).roundToInt(),
            proteinGrams = proteinPer100g * scale,
            carbGrams = carbsPer100g * scale,
            fatGrams = fatPer100g * scale
        )
    }
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
