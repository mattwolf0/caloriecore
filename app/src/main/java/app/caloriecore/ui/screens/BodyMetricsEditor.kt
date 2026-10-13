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
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.components.keepLogNumberText
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.CalorieSource
import app.caloriecore.ui.model.Sex
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber
import app.caloriecore.ui.theme.GymGreen

@Composable
fun BodyMetricsEditor(
    profile: BodySnapshot,
    strings: CalorieCoreStrings,
    onProfileChange: (BodySnapshot) -> Unit,
    phoneStepsOn: Boolean = false
) {
    var bodyCardOpen by rememberSaveable(profile.id, profile.loggedAt) { mutableStateOf(false) }
    var draft by rememberSaveable(profile.id, profile.loggedAt) { mutableStateOf(BodyDraft(profile)) }

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
                    TextButton(onClick = {
                        draft = BodyDraft(profile)
                        bodyCardOpen = true
                    }) { Text(strings.edit) }
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
                        selected = draft.sex == sex,
                        onClick = { draft = draft.copy(sex = sex) },
                        label = { Text(strings.sexDisplayName(sex)) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInput(
                    label = strings.age,
                    numberText = draft.age,
                    onValueChange = { typedAge ->
                        draft = draft.copy(age = keepLogNumberText(typedAge))
                    },
                    modifier = Modifier.weight(1f)
                )
                NumberInput(
                    label = strings.height,
                    numberText = draft.height,
                    onValueChange = { typedHeight ->
                        draft = draft.copy(height = keepLogNumberText(typedHeight))
                    },
                    modifier = Modifier.weight(1f),
                    suffix = "cm"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInput(
                    label = strings.weight,
                    numberText = draft.weight,
                    onValueChange = { typedWeight ->
                        draft = draft.copy(weight = keepLogNumberText(typedWeight), weightEdited = true)
                    },
                    modifier = Modifier.weight(1f),
                    suffix = "kg",
                    allowDecimal = true
                )
                NumberInput(
                    label = strings.bodyFat,
                    numberText = draft.bodyFat,
                    onValueChange = { typedBodyFat ->
                        draft = draft.copy(bodyFat = keepLogNumberText(typedBodyFat))
                    },
                    modifier = Modifier.weight(1f),
                    suffix = "%",
                    allowDecimal = true
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInput(
                    label = strings.sleep,
                    numberText = draft.sleep,
                    onValueChange = { typedSleep ->
                        draft = draft.copy(sleep = keepLogNumberText(typedSleep))
                    },
                    modifier = Modifier.weight(1f),
                    suffix = "h",
                    allowDecimal = true
                )
                NumberInput(
                    label = strings.restingHeartRate,
                    numberText = draft.pulse,
                    onValueChange = { typedPulse ->
                        draft = draft.copy(pulse = keepLogNumberText(typedPulse))
                    },
                    modifier = Modifier.weight(1f),
                    suffix = "bpm"
                )
            }
            Text(strings.calorieSource, style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalorieSource.entries.forEach { source ->
                    FilterChip(
                        selected = draft.calorieSource == source,
                        onClick = { draft = draft.copy(calorieSource = source) },
                        label = { Text(if (source == CalorieSource.Watch) strings.watch else strings.loggedActivity) }
                    )
                }
            }
            Text(strings.calorieSourceHint, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInput(
                    label = strings.activeCalories,
                    numberText = draft.watchCalories,
                    onValueChange = { typedActiveKcal ->
                        draft = draft.copy(watchCalories = keepLogNumberText(typedActiveKcal))
                    },
                    modifier = Modifier.weight(1f)
                )
                NumberInput(
                    label = strings.steps,
                    numberText = if (phoneStepsOn) profile.steps.toString() else draft.steps,
                    onValueChange = { typedSteps ->
                        draft = draft.copy(steps = keepLogNumberText(typedSteps))
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !phoneStepsOn
                )
            }
            NumberInput(
                label = strings.trainingCalories,
                numberText = draft.trainingCalories,
                onValueChange = { typedTrainingKcal ->
                    draft = draft.copy(trainingCalories = keepLogNumberText(typedTrainingKcal))
                }
            )
            if (!draft.canSave) {
                Text(strings.profileSetupInvalid, color = MaterialTheme.colorScheme.error)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SolidActionButton(
                    text = strings.saveChanges,
                    enabled = draft.canSave,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        draft.buildProfile(profile)?.let(onProfileChange)
                        bodyCardOpen = false
                    }
                )
                TextButton(onClick = { bodyCardOpen = false }) { Text(strings.cancel) }
            }
        }
    }
}

@Composable
private fun ProfileMetric(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
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
