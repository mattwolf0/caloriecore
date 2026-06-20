package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BurnEstimate
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
internal fun BurnBreakdownCard(burn: BurnEstimate, strings: CalorieCoreStrings) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShelfHeader(strings.breakdown)
            BurnLineItem(strings.baseMetabolism, burn.bmr)
            BurnLineItem(strings.watchActivity, burn.activeCalories)
            BurnLineItem(strings.plannedWorkout, burn.plannedTraining)
            BurnLineItem("TEF", burn.tef)
            BurnLineItem(strings.sleepCorrection, burn.sleepAdjustment)
        }
    }
}

@Composable
private fun BurnLineItem(label: String, kcalDelta: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "${if (kcalDelta > 0) "+" else ""}${CalorieCoreFormatter.kcal(kcalDelta)}",
            fontWeight = FontWeight.SemiBold
        )
    }
}
