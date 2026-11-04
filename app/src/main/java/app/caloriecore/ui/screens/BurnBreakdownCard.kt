package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BurnEstimate
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing

@Composable
internal fun BurnBreakdownCard(burn: BurnEstimate, strings: CalorieCoreStrings) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
            ShelfHeader(strings.breakdown)
            BurnLineItem(strings.baseMetabolism, burn.bmr, burn.total, MaterialTheme.colorScheme.primary)
            BurnLineItem(strings.watchActivity, burn.activeCalories, burn.total, MaterialTheme.colorScheme.secondary)
            BurnLineItem(strings.plannedWorkout, burn.plannedTraining, burn.total, MaterialTheme.colorScheme.tertiary)
            BurnLineItem(strings.loggedActivity, burn.loggedActivityCalories, burn.total, MaterialTheme.colorScheme.primary)
            BurnLineItem(strings.foodDigestion, burn.tef, burn.total, MaterialTheme.colorScheme.secondary)
            BurnLineItem(strings.sleepCorrection, burn.sleepAdjustment, burn.total, MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun BurnLineItem(label: String, kcalDelta: Int, total: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
            Text(text = label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "${if (kcalDelta > 0) "+" else ""}${CalorieCoreFormatter.kcal(kcalDelta)}",
                fontWeight = FontWeight.SemiBold
            )
        }
        if (kcalDelta > 0 && total > 0) {
            LinearProgressIndicator(
                progress = { (kcalDelta.toFloat() / total).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = color,
                trackColor = color.copy(alpha = 0.12f),
                drawStopIndicator = {}
            )
        }
    }
}
