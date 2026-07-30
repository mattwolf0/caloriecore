package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    strings: CalorieCoreStrings
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
                text = state.steps.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = GymGreen,
                fontWeight = FontWeight.Bold
            )
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
