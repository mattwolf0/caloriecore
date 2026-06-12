package app.caloriecore.ui.model

import kotlin.math.pow
import kotlin.math.roundToInt

private const val LowestPlausibleBodyFat = 2.0
private const val HighestPlausibleBodyFat = 80.0

enum class SleepQuality {
    VeryLow,
    Low,
    Stable,
    High
}

enum class BmiCategory {
    Low,
    Normal,
    Elevated,
    High
}

data class BurnEstimate(
    val bmr: Int,
    val bmi: Double,
    val tef: Int,
    val sleepAdjustment: Int,
    val activeCalories: Int,
    val plannedTraining: Int,
    val total: Int,
    val sleepQuality: SleepQuality,
    val bmiCategory: BmiCategory,
    val usesBodyFatFormula: Boolean
)

data class PickedDayReport(
    val foodEntries: List<FoodEntry>,
    val sessions: List<TrainingSession>,
    val profile: BodySnapshot,
    val nutrition: NutritionTotals,
    val burnEstimate: BurnEstimate,
    val workoutVolumeKg: Double,
    val workoutSets: Int
)

fun burnEstimateForDay(profile: BodySnapshot, foodEntries: List<FoodEntry>): BurnEstimate {
    val nutrition = sumPlateMacros(foodEntries)
    val heightMeters = profile.heightCm / 100.0
    val bmi = if (heightMeters > 0.0) profile.weightKg / heightMeters.pow(2.0) else 0.0
    val bodyFat = profile.bodyFatForBmr()
    val bmr = if (bodyFat != null) {
        val leanMassKg = profile.weightKg * (1.0 - bodyFat / 100.0)
        (370.0 + 21.6 * leanMassKg).roundToInt()
    } else {
        val sexOffset = if (profile.sex == Sex.Male) 5 else -161
        (10 * profile.weightKg + 6.25 * profile.heightCm - 5 * profile.age + sexOffset).roundToInt()
    }
    val tef = (nutrition.calories * 0.1).roundToInt()
    val sleepAdjustment = when {
        profile.sleepHours < 5.5 -> -140
        profile.sleepHours < 6.5 -> -80
        profile.sleepHours > 9.5 -> -40
        else -> 0
    }
    val total = bmr + profile.watchActiveCalories + profile.plannedWorkoutCalories + tef + sleepAdjustment
    return BurnEstimate(
        bmr = bmr,
        bmi = bmi,
        tef = tef,
        sleepAdjustment = sleepAdjustment,
        activeCalories = profile.watchActiveCalories,
        plannedTraining = profile.plannedWorkoutCalories,
        total = total,
        sleepQuality = when {
            profile.sleepHours < 5.5 -> SleepQuality.VeryLow
            profile.sleepHours < 6.5 -> SleepQuality.Low
            profile.sleepHours <= 9.0 -> SleepQuality.Stable
            else -> SleepQuality.High
        },
        bmiCategory = when {
            bmi < 18.5 -> BmiCategory.Low
            bmi < 25.0 -> BmiCategory.Normal
            bmi < 30.0 -> BmiCategory.Elevated
            else -> BmiCategory.High
        },
        usesBodyFatFormula = bodyFat != null
    )
}

private fun BodySnapshot.bodyFatForBmr(): Double? =
    bodyFatPercent?.takeIf { it in LowestPlausibleBodyFat..HighestPlausibleBodyFat }

fun Logbook.pickedDayReport(): PickedDayReport {
    val dayMeals = foodRowsForPickedDay()
    val daySessions = gymRowsForPickedDay()
    val dayProfile = nearestBodyCheckIn(bodyHistory, selectedDateTime)
    return PickedDayReport(
        foodEntries = dayMeals,
        sessions = daySessions,
        profile = dayProfile,
        nutrition = sumPlateMacros(dayMeals),
        burnEstimate = burnEstimateForDay(dayProfile, dayMeals),
        workoutVolumeKg = daySessions.sumOf { it.totalVolumeKg },
        workoutSets = daySessions.sumOf { it.totalSets }
    )
}
