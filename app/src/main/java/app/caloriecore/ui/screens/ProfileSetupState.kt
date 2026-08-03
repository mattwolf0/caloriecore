package app.caloriecore.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.caloriecore.ui.components.keepLogNumberText
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.Sex

internal class ProfileSetupState internal constructor(
    private val sexState: MutableState<Sex?>,
    private val ageInputState: MutableState<String>,
    private val heightInputState: MutableState<String>,
    private val weightInputState: MutableState<String>,
    private val bodyFatInputState: MutableState<String>
) {
    var sex by sexState
    var ageInput by ageInputState
        private set
    var heightInput by heightInputState
        private set
    var weightInput by weightInputState
        private set
    var bodyFatInput by bodyFatInputState
        private set

    val ageInvalid: Boolean
        get() = ageInput.isNotBlank() && !ageInput.isIntIn(ValidAge)

    val heightInvalid: Boolean
        get() = heightInput.isNotBlank() && !heightInput.isIntIn(ValidHeightCm)

    val weightInvalid: Boolean
        get() = weightInput.isNotBlank() && !weightInput.isDoubleIn(ValidWeightKg)

    val bodyFatInvalid: Boolean
        get() = bodyFatInput.isNotBlank() && !bodyFatInput.isDoubleIn(ValidBodyFat)

    val hasInvalidValues: Boolean
        get() = ageInvalid || heightInvalid || weightInvalid || bodyFatInvalid

    val canSave: Boolean
        get() = sex != null &&
            ageInput.isIntIn(ValidAge) &&
            heightInput.isIntIn(ValidHeightCm) &&
            weightInput.isDoubleIn(ValidWeightKg) &&
            (bodyFatInput.isBlank() || bodyFatInput.isDoubleIn(ValidBodyFat))

    fun updateAge(nextValue: String) {
        ageInput = keepLogNumberText(nextValue)
    }

    fun updateHeight(nextValue: String) {
        heightInput = keepLogNumberText(nextValue)
    }

    fun updateWeight(nextValue: String) {
        weightInput = keepLogNumberText(nextValue, allowDecimal = true)
    }

    fun updateBodyFat(nextValue: String) {
        bodyFatInput = keepLogNumberText(nextValue, allowDecimal = true)
    }

    fun buildProfile(loggedAt: Long): BodySnapshot {
        require(canSave)
        return BodySnapshot(
            loggedAt = loggedAt,
            sex = checkNotNull(sex),
            age = ageInput.toInt(),
            heightCm = heightInput.toInt(),
            weightKg = weightInput.toDouble(),
            bodyFatPercent = bodyFatInput.toDoubleOrNull()
        )
    }
}

@Composable
internal fun rememberProfileSetupState(): ProfileSetupState {
    val sex = rememberSaveable { mutableStateOf<Sex?>(null) }
    val ageInput = rememberSaveable { mutableStateOf("") }
    val heightInput = rememberSaveable { mutableStateOf("") }
    val weightInput = rememberSaveable { mutableStateOf("") }
    val bodyFatInput = rememberSaveable { mutableStateOf("") }

    return remember {
        ProfileSetupState(
            sex,
            ageInput,
            heightInput,
            weightInput,
            bodyFatInput
        )
    }
}

private val ValidAge = 13..120
private val ValidHeightCm = 100..250
private val ValidWeightKg = 25.0..400.0
private val ValidBodyFat = 2.0..80.0

private fun String.isIntIn(range: IntRange): Boolean = toIntOrNull()?.let { it in range } == true

private fun String.isDoubleIn(range: ClosedFloatingPointRange<Double>): Boolean =
    toDoubleOrNull()?.let { it in range } == true
