package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.caloriecore.ui.components.InputRow
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MetricValue
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BurnEstimate
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing
import kotlin.math.roundToInt

@Composable
internal fun DailyBurnCard(
    burn: BurnEstimate,
    intakeCalories: Int,
    strings: CalorieCoreStrings,
    hasFood: Boolean = true,
    showDetails: Boolean = false,
    onToggleDetails: () -> Unit = {}
) {
    val ratio = if (burn.total > 0) intakeCalories.toFloat() / burn.total else 0f
    val color = if (ratio > 1f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
            Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.tight)) {
                Text(strings.dailyBalance, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (hasFood) CalorieCoreFormatter.kcal(intakeCalories - burn.total) else "—",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (hasFood) strings.balanceHint else strings.noMealsLogged,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            InputRow {
                MetricValue(strings.intake, CalorieCoreFormatter.kcal(intakeCalories), Modifier.weight(1f))
                MetricValue(strings.estimatedBurn, CalorieCoreFormatter.kcal(burn.total), Modifier.weight(1f))
            }
            if (hasFood && burn.total > 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(strings.burnShare, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                    Text("${(ratio * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = color)
                }
                LinearProgressIndicator(
                    progress = { ratio.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = color,
                    drawStopIndicator = {}
                )
            }
            TextButton(onClick = onToggleDetails, modifier = Modifier.fillMaxWidth()) {
                Text(if (showDetails) strings.hideDetails else strings.showDetails)
            }
        }
    }
}
