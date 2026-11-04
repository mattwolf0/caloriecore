package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.caloriecore.ui.components.EditorActions
import app.caloriecore.ui.components.InputRow
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.keepLogNumberText
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.Sex
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing

@Composable
internal fun ProfileSummary(profile: BodySnapshot, strings: CalorieCoreStrings, onEdit: () -> Unit) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(strings.profileData, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onEdit, shape = MaterialTheme.shapes.small) { Text(strings.edit) }
            }
            InputRow {
                ProfileMetric(strings.weight, CalorieCoreFormatter.kilograms(profile.weightKg), Modifier.weight(1f))
                ProfileMetric(strings.height, "${profile.heightCm} cm", Modifier.weight(1f))
            }
            InputRow {
                ProfileMetric(strings.age, profile.age.toString(), Modifier.weight(1f))
                ProfileMetric(strings.bodyFat, profile.bodyFatPercent?.let { "${CalorieCoreFormatter.logDecimal(it)}%" } ?: "—", Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun BodyMetricsEditor(
    draft: BodyDraft,
    strings: CalorieCoreStrings,
    onChange: (BodyDraft) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
        ScreenName(strings.profileData)
        LogCard {
            Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
                InputRow {
                    Sex.entries.forEach { sex ->
                        FilterChip(draft.sex == sex, { onChange(draft.copy(sex = sex)) }, { Text(strings.sexDisplayName(sex)) })
                    }
                }
                InputRow {
                    NumberInput(
                        label = strings.age,
                        numberText = draft.age,
                        onValueChange = { onChange(draft.copy(age = keepLogNumberText(it))) },
                        modifier = Modifier.weight(1f)
                    )
                    NumberInput(
                        label = strings.height,
                        numberText = draft.height,
                        onValueChange = { onChange(draft.copy(height = keepLogNumberText(it))) },
                        modifier = Modifier.weight(1f),
                        suffix = "cm"
                    )
                }
                InputRow {
                    NumberInput(
                        label = strings.weight,
                        numberText = draft.weight,
                        onValueChange = { onChange(draft.copy(weight = keepLogNumberText(it, true))) },
                        modifier = Modifier.weight(1f),
                        suffix = "kg",
                        allowDecimal = true
                    )
                    NumberInput(
                        label = strings.bodyFat,
                        numberText = draft.bodyFat,
                        onValueChange = { onChange(draft.copy(bodyFat = keepLogNumberText(it, true))) },
                        modifier = Modifier.weight(1f),
                        suffix = "%",
                        allowDecimal = true
                    )
                }
                Text(strings.profileSetupBodyFatHint, style = MaterialTheme.typography.bodySmall)
            }
        }
        LogCard {
            Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
                InputRow {
                    NumberInput(
                        label = strings.sleep,
                        numberText = draft.sleep,
                        onValueChange = { onChange(draft.copy(sleep = keepLogNumberText(it, true))) },
                        modifier = Modifier.weight(1f),
                        suffix = "h",
                        allowDecimal = true
                    )
                    NumberInput(
                        label = strings.restingHeartRate,
                        numberText = draft.pulse,
                        onValueChange = { onChange(draft.copy(pulse = keepLogNumberText(it))) },
                        modifier = Modifier.weight(1f),
                        suffix = "bpm"
                    )
                }
                InputRow {
                    NumberInput(
                        label = strings.watchActivity,
                        numberText = draft.activeKcal,
                        onValueChange = { onChange(draft.copy(activeKcal = keepLogNumberText(it))) },
                        modifier = Modifier.weight(1f),
                        suffix = "kcal"
                    )
                    NumberInput(
                        label = strings.steps,
                        numberText = draft.steps,
                        onValueChange = { onChange(draft.copy(steps = keepLogNumberText(it))) },
                        modifier = Modifier.weight(1f)
                    )
                }
                NumberInput(
                    label = strings.trainingCalories,
                    numberText = draft.trainingKcal,
                    onValueChange = { onChange(draft.copy(trainingKcal = keepLogNumberText(it))) },
                    suffix = "kcal"
                )
                if (!draft.canSave) Text(strings.profileSetupInvalid, color = MaterialTheme.colorScheme.error)
            }
        }
        EditorActions(
            saveText = strings.save,
            canSave = draft.canSave,
            strings = strings,
            onSave = onSave,
            onCancel = onCancel
        )
    }
}

@Composable
private fun ProfileMetric(label: String, value: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(UiSpacing.tight)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
