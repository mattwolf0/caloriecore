package app.caloriecore.ui.model

import kotlin.math.roundToInt

data class MacroTarget(
    val goal: NutritionGoal,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int
)

fun macroTargetFor(
    profile: BodySnapshot,
    activities: List<ActivityEntry>,
    goal: NutritionGoal
): MacroTarget {
    val burn = burnEstimateForDay(profile, emptyList(), activities)
    val calories = (burn.total + goal.calorieChange()).coerceAtLeast(1_200)
    val proteinPerKg = when (goal) {
        NutritionGoal.BuildMuscle -> 1.8
        NutritionGoal.LoseFat -> 2.2
        NutritionGoal.MaintainWeight -> 1.6
    }
    val fatPerKg = when (goal) {
        NutritionGoal.LoseFat -> 0.7
        NutritionGoal.BuildMuscle,
        NutritionGoal.MaintainWeight -> 0.8
    }
    val protein = (profile.weightKg * proteinPerKg).roundToInt()
    val fat = (profile.weightKg * fatPerKg).roundToInt()
    val carbs = ((calories - protein * 4 - fat * 9).coerceAtLeast(0) / 4.0).roundToInt()

    return MacroTarget(
        goal = goal,
        calories = calories,
        proteinGrams = protein,
        carbsGrams = carbs,
        fatGrams = fat
    )
}

private fun NutritionGoal.calorieChange(): Int = when (this) {
    NutritionGoal.BuildMuscle -> 250
    NutritionGoal.LoseFat -> -400
    NutritionGoal.MaintainWeight -> 0
}
