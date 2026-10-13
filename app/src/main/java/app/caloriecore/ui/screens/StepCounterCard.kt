package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.model.PhoneStepState
import app.caloriecore.ui.model.PhoneStepStatus
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.GymGreen

@Composable
fun StepCounterCard(
    state: PhoneStepState,
    strings: CalorieCoreStrings,
    usePhoneSteps: Boolean,
    manualSteps: Int,
    onPhoneStepsChange: (Boolean) -> Unit,
    onPermission: () -> Unit,
    onSettings: () -> Unit,
    onRetry: () -> Unit
) {
    val detail = when (state.status) {
        PhoneStepStatus.Starting -> strings.phoneStepsStarting
        PhoneStepStatus.Active -> null
        PhoneStepStatus.PermissionNeeded -> strings.stepPermissionNeeded
        PhoneStepStatus.NotAvailable -> strings.stepCounterUnavailable
        PhoneStepStatus.Error -> strings.stepCounterError
    }

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = strings.steps,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = (if (usePhoneSteps) state.steps else manualSteps).toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = GymGreen,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(strings.usePhoneSteps, modifier = Modifier.weight(1f))
                Switch(checked = usePhoneSteps, onCheckedChange = onPhoneStepsChange)
            }
            if (!usePhoneSteps) Text(strings.manualSteps, style = MaterialTheme.typography.bodySmall)
            if (usePhoneSteps && detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (usePhoneSteps && state.status == PhoneStepStatus.PermissionNeeded) {
                Row {
                    TextButton(onClick = onPermission) { Text(strings.allowSteps) }
                    TextButton(onClick = onSettings) { Text(strings.openSettings) }
                }
            }
            if (usePhoneSteps && state.status == PhoneStepStatus.Error) {
                TextButton(onClick = onRetry) { Text(strings.tryAgain) }
            }
        }
    }
}
