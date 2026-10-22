package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.PickRow
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenName(title = strings.profileSetupTitle) }
        item {
            Text(
                text = strings.profileSetupIntro,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = strings.profileSetupRequired,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${strings.gender} *",
                        style = MaterialTheme.typography.titleSmall
                    )
                    PickRow(
                        options = Sex.entries,
                        selected = setup.sex,
                        label = strings::sexDisplayName,
                        onSelect = { setup.sex = it }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
    }
}
