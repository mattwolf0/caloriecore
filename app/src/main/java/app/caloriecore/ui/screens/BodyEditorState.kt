package app.caloriecore.ui.screens

import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.CalorieSource
import app.caloriecore.ui.model.Sex
import app.caloriecore.ui.model.canBeSaved
import java.io.Serializable

internal data class BodyDraft(
    val profile: BodySnapshot,
    val sex: Sex = profile.sex,
    val age: String = profile.age.toString(),
    val height: String = profile.heightCm.toString(),
    val weight: String = CalorieCoreFormatter.logInput(profile.weightKg),
    val bodyFat: String = profile.bodyFatPercent?.let(CalorieCoreFormatter::logInput).orEmpty(),
    val sleep: String = profile.sleepHours.takeIf { it > 0.0 }?.let(CalorieCoreFormatter::logInput).orEmpty(),
    val watchCalories: String = profile.watchActiveCalories.toString(),
    val steps: String = profile.steps.toString(),
    val trainingCalories: String = profile.plannedWorkoutCalories.toString(),
    val pulse: String = profile.restingHeartRate.toString(),
    val calorieSource: CalorieSource = profile.calorieSource,
    val weightEdited: Boolean = false
) : Serializable {
    val canSave: Boolean get() = buildProfile() != null

    fun buildProfile(current: BodySnapshot = profile): BodySnapshot? {
        val next = current.copy(
            sex = sex,
            age = age.toIntOrNull() ?: return null,
            heightCm = height.toIntOrNull() ?: return null,
            weightKg = weight.toDoubleOrNull() ?: return null,
            bodyFatPercent = if (bodyFat.isBlank()) null else bodyFat.toDoubleOrNull() ?: return null,
            sleepHours = if (sleep.isBlank()) 0.0 else sleep.toDoubleOrNull() ?: return null,
            watchActiveCalories = watchCalories.toIntOrNull() ?: return null,
            steps = steps.toIntOrNull() ?: return null,
            plannedWorkoutCalories = trainingCalories.toIntOrNull() ?: return null,
            restingHeartRate = pulse.toIntOrNull() ?: return null,
            calorieSource = calorieSource,
            hasWeightEntry = current.hasWeightEntry || weightEdited
        )
        return next.takeIf { it.canBeSaved() }
    }
}
