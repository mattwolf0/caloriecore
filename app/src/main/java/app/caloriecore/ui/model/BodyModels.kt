package app.caloriecore.ui.model

import java.io.Serializable

private const val SoloProfileId = "local-user"

enum class Sex {
    Male,
    Female
}

enum class CalorieSource {
    Watch,
    Logs
}

data class BodySnapshot(
    val id: Long = newLogId(),
    val loggedAt: Long = phoneNowMillis(),
    val userId: String = SoloProfileId,
    val sex: Sex = Sex.Male,
    val age: Int = 29,
    val heightCm: Int = 178,
    val weightKg: Double = 82.0,
    val bodyFatPercent: Double? = null,
    val sleepHours: Double = 0.0,
    val watchActiveCalories: Int = 0,
    val steps: Int = 0,
    val plannedWorkoutCalories: Int = 0,
    val restingHeartRate: Int = 60,
    val calorieSource: CalorieSource = CalorieSource.Logs,
    val hasWeightEntry: Boolean = true
) : Serializable

fun BodySnapshot.canBeSaved(): Boolean = age in 13..120 && heightCm in 100..250 &&
    weightKg.isFinite() && weightKg in 25.0..400.0 &&
    (bodyFatPercent == null || (bodyFatPercent.isFinite() && bodyFatPercent in 2.0..80.0)) &&
    sleepHours.isFinite() && sleepHours in 0.0..24.0 &&
    watchActiveCalories in 0..100_000 && plannedWorkoutCalories in 0..100_000 &&
    steps in 0..1_000_000 && restingHeartRate in 20..250
