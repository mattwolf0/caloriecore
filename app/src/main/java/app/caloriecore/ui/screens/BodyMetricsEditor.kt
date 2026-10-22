package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.FormSheet
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.PickRow
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.components.StatCell
import app.caloriecore.ui.components.keepLogNumberText
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.CalorieSource
import app.caloriecore.ui.model.Sex
import app.caloriecore.ui.text.CalorieCoreStrings

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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCell(strings.weight, CalorieCoreFormatter.kilograms(profile.weightKg))
                StatCell(strings.height, "${profile.heightCm} cm")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCell(strings.sleep, "${CalorieCoreFormatter.logDecimal(profile.sleepHours)} h")
                StatCell(strings.restingHeartRate, CalorieCoreFormatter.bpm(profile.restingHeartRate))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                StatCell(strings.steps, profile.steps.toString())
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        draft = BodyDraft(profile)
                        bodyCardOpen = true
                    }) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.edit)
                    }
                }
            }
        }
    }

    if (!bodyCardOpen) return

    FormSheet(
        title = strings.profileData,
        closeText = strings.close,
        onDismiss = { bodyCardOpen = false }
    ) {
        PickRow(
            options = Sex.entries,
            selected = draft.sex,
            label = strings::sexDisplayName,
            onSelect = { draft = draft.copy(sex = it) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        Text(
            text = strings.calorieSource,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        PickRow(
            options = CalorieSource.entries,
            selected = draft.calorieSource,
            label = { source -> if (source == CalorieSource.Watch) strings.watch else strings.loggedActivity },
            onSelect = { draft = draft.copy(calorieSource = it) }
        )
        Text(
            text = strings.calorieSourceHint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        SolidActionButton(
            text = strings.saveChanges,
            enabled = draft.canSave,
            onClick = {
                draft.buildProfile(profile)?.let(onProfileChange)
                bodyCardOpen = false
            }
        )
    }
}
