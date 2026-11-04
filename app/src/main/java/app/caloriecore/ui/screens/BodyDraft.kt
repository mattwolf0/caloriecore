package app.caloriecore.ui.screens

import androidx.compose.runtime.saveable.listSaver
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.Sex

internal data class BodyDraft(
    val sex: Sex,
    val age: String,
    val height: String,
    val weight: String,
    val bodyFat: String,
    val sleep: String,
    val pulse: String,
    val activeKcal: String,
    val steps: String,
    val trainingKcal: String
) {
    val canSave: Boolean
        get() = age.toIntOrNull() in 13..120 && height.toIntOrNull() in 100..250 &&
            weight.toDoubleOrNull()?.let { it in 25.0..400.0 } == true &&
            (bodyFat.isBlank() || bodyFat.toDoubleOrNull()?.let { it in 2.0..80.0 } == true) &&
            sleep.toDoubleOrNull()?.let { it in 0.0..24.0 } == true &&
            pulse.toIntOrNull() in 1..250 &&
            listOf(activeKcal, steps, trainingKcal).all { (it.toIntOrNull() ?: -1) >= 0 }

    fun buildProfile(profile: BodySnapshot): BodySnapshot {
        require(canSave)
        return profile.copy(
            sex = sex,
            age = age.toInt(),
            heightCm = height.toInt(),
            weightKg = weight.toDouble(),
            bodyFatPercent = bodyFat.toDoubleOrNull(),
            sleepHours = sleep.toDouble(),
            restingHeartRate = pulse.toInt(),
            watchActiveCalories = activeKcal.toInt(),
            steps = steps.toInt(),
            plannedWorkoutCalories = trainingKcal.toInt()
        )
    }
}

internal fun BodySnapshot.toBodyDraft() = BodyDraft(
    sex = sex,
    age = age.toString(),
    height = heightCm.toString(),
    weight = CalorieCoreFormatter.logDecimal(weightKg),
    bodyFat = bodyFatPercent?.let(CalorieCoreFormatter::logDecimal).orEmpty(),
    sleep = CalorieCoreFormatter.logDecimal(sleepHours),
    pulse = restingHeartRate.toString(),
    activeKcal = watchActiveCalories.toString(),
    steps = steps.toString(),
    trainingKcal = plannedWorkoutCalories.toString()
)

internal val BodyDraftSaver = listSaver<BodyDraft, String>(
    save = {
        listOf(
            it.sex.name,
            it.age,
            it.height,
            it.weight,
            it.bodyFat,
            it.sleep,
            it.pulse,
            it.activeKcal,
            it.steps,
            it.trainingKcal
        )
    },
    restore = {
        BodyDraft(
            sex = Sex.valueOf(it[0]),
            age = it[1],
            height = it[2],
            weight = it[3],
            bodyFat = it[4],
            sleep = it[5],
            pulse = it[6],
            activeKcal = it[7],
            steps = it[8],
            trainingKcal = it[9]
        )
    }
)
