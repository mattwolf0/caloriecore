package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.Sex
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
fun ProfileSetupScreen(
    selectedDateTime: Long,
    strings: CalorieCoreStrings,
    onComplete: (BodySnapshot) -> Unit,
    modifier: Modifier = Modifier
) {
    val setup = rememberProfileSetupState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenName(title = strings.profileSetupTitle) }
        item {
            Text(
                text = strings.profileSetupIntro,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = strings.profileData,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.profileSetupRequired,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${strings.gender} *",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Sex.entries.forEach { sex ->
                            FilterChip(
                                selected = setup.sex == sex,
                                onClick = { setup.sex = sex },
                                label = { Text(strings.sexDisplayName(sex)) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberInput(
                            label = "${strings.age} *",
                            numberText = setup.ageInput,
                            onValueChange = setup::updateAge,
                            modifier = Modifier.weight(1f),
                            isError = setup.ageInvalid
                        )
                        NumberInput(
                            label = "${strings.height} *",
                            numberText = setup.heightInput,
                            onValueChange = setup::updateHeight,
                            modifier = Modifier.weight(1f),
                            suffix = "cm",
                            isError = setup.heightInvalid
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberInput(
                            label = "${strings.weight} *",
                            numberText = setup.weightInput,
                            onValueChange = setup::updateWeight,
                            modifier = Modifier.weight(1f),
                            suffix = "kg",
                            allowDecimal = true,
                            isError = setup.weightInvalid
                        )
                        NumberInput(
                            label = strings.bodyFat,
                            numberText = setup.bodyFatInput,
                            onValueChange = setup::updateBodyFat,
                            modifier = Modifier.weight(1f),
                            suffix = "%",
                            allowDecimal = true,
                            isError = setup.bodyFatInvalid
                        )
                    }
                    Text(
                        text = strings.profileSetupBodyFatHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (setup.hasInvalidValues) {
                        Text(
                            text = strings.profileSetupInvalid,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    SolidActionButton(
                        text = strings.profileSetupContinue,
                        enabled = setup.canSave,
                        onClick = { onComplete(setup.buildProfile(selectedDateTime)) }
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}
