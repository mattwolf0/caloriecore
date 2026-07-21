package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.keepLogNumberText
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.Sex
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber
import app.caloriecore.ui.theme.GymGreen

@Composable
fun BodyMetricsEditor(profile: BodySnapshot, strings: CalorieCoreStrings, onProfileChange: (BodySnapshot) -> Unit) {
    var bodyCardOpen by rememberSaveable(profile.loggedAt) { mutableStateOf(false) }

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!bodyCardOpen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileMetric(
                                label = strings.weight,
                                value = CalorieCoreFormatter.kilograms(profile.weightKg),
                                color = GymGreen,
                                modifier = Modifier.weight(1f)
                            )
                            ProfileMetric(
                                label = strings.height,
                                value = "${profile.heightCm} cm",
                                color = FoodAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileMetric(
                                label = strings.steps,
                                value = profile.steps.toString(),
                                color = GymGreen,
                                modifier = Modifier.weight(1f)
                            )
                            ProfileMetric(
                                label = strings.restingHeartRate,
                                value = CalorieCoreFormatter.bpm(profile.restingHeartRate),
                                color = FoodAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    TextButton(onClick = { bodyCardOpen = true }) { Text(strings.edit) }
                }
                return@Column
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = strings.profileData,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { bodyCardOpen = false }) { Text(strings.close) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sex.entries.forEach { sex ->
                    FilterChip(
                        selected = profile.sex == sex,
                        onClick = { onProfileChange(profile.copy(sex = sex)) },
                        label = { Text(strings.sexDisplayName(sex)) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInput(
                    label = strings.age,
                    numberText = profile.age.toString(),
                    onValueChange = { typedAge ->
                        keepLogNumberText(typedAge).toIntOrNull()?.let { onProfileChange(profile.copy(age = it)) }
                    },
                    modifier = Modifier.weight(1f)
                )
                NumberInput(
                    label = strings.height,
                    numberText = profile.heightCm.toString(),
                    onValueChange = { typedHeight ->
                        keepLogNumberText(typedHeight).toIntOrNull()?.let { onProfileChange(profile.copy(heightCm = it)) }
                    },
                    modifier = Modifier.weight(1f),
                    suffix = "cm"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInput(
                    label = strings.weight,
                    numberText = CalorieCoreFormatter.logDecimal(profile.weightKg),
                    onValueChange = { typedWeight ->
                        keepLogNumberText(typedWeight, allowDecimal = true).toDoubleOrNull()?.let {
                            onProfileChange(profile.copy(weightKg = it))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    suffix = "kg",
                    allowDecimal = true
                )
                NumberInput(
                    label = strings.bodyFat,
                    numberText = profile.bodyFatPercent?.let(CalorieCoreFormatter::logDecimal) ?: "",
                    onValueChange = { typedBodyFat ->
                        val cleaned = keepLogNumberText(typedBodyFat, allowDecimal = true)
                        onProfileChange(profile.copy(bodyFatPercent = cleaned.toDoubleOrNull()))
                    },
                    modifier = Modifier.weight(1f),
                    suffix = "%",
                    allowDecimal = true
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInput(
                    label = strings.sleep,
                    numberText = CalorieCoreFormatter.logDecimal(profile.sleepHours),
                    onValueChange = { typedSleep ->
                        keepLogNumberText(typedSleep, allowDecimal = true).toDoubleOrNull()?.let {
                            onProfileChange(profile.copy(sleepHours = it))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    suffix = "h",
                    allowDecimal = true
                )
                NumberInput(
                    label = strings.restingHeartRate,
                    numberText = profile.restingHeartRate.toString(),
                    onValueChange = { typedPulse ->
                        keepLogNumberText(typedPulse).toIntOrNull()?.let {
                            onProfileChange(profile.copy(restingHeartRate = it))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    suffix = "bpm"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInput(
                    label = strings.activeCalories,
                    numberText = profile.watchActiveCalories.toString(),
                    onValueChange = { typedActiveKcal ->
                        keepLogNumberText(typedActiveKcal).toIntOrNull()?.let {
                            onProfileChange(profile.copy(watchActiveCalories = it))
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                NumberInput(
                    label = strings.steps,
                    numberText = profile.steps.toString(),
                    onValueChange = { typedSteps ->
                        keepLogNumberText(typedSteps).toIntOrNull()?.let { onProfileChange(profile.copy(steps = it)) }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            NumberInput(
                label = strings.trainingCalories,
                numberText = profile.plannedWorkoutCalories.toString(),
                onValueChange = { typedTrainingKcal ->
                    keepLogNumberText(typedTrainingKcal).toIntOrNull()?.let {
                        onProfileChange(profile.copy(plannedWorkoutCalories = it))
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
