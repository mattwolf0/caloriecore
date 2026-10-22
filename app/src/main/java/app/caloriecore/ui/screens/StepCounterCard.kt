package app.caloriecore.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.model.PhoneStepState
import app.caloriecore.ui.model.PhoneStepStatus
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.LineIcons

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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(LineIcons.Steps, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.steps,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = (if (usePhoneSteps) state.steps else manualSteps).toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.usePhoneSteps, style = MaterialTheme.typography.bodyMedium)
                    val hint = if (!usePhoneSteps) strings.manualSteps else detail
                    if (hint != null) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(checked = usePhoneSteps, onCheckedChange = onPhoneStepsChange)
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
