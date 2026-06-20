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
import app.caloriecore.ui.components.MiniBadge
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.BurnEstimate
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber
import app.caloriecore.ui.theme.GymGreen
import app.caloriecore.ui.theme.WarningRed

@Composable
internal fun DailyBurnCard(
    burn: BurnEstimate,
    profile: BodySnapshot,
    strings: CalorieCoreStrings
) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = strings.dailyBurn,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CalorieCoreFormatter.kcal(burn.total),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                MiniBadge(
                    text = "${strings.bmi} ${CalorieCoreFormatter.logDecimal(burn.bmi)}",
                    color = GymGreen
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                MiniBadge("${strings.sleep}: ${strings.sleepQualityDisplayName(burn.sleepQuality)}")
                MiniBadge("TEF ${CalorieCoreFormatter.kcal(burn.tef)}", color = FoodAmber)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                val bmrRecipe = if (burn.usesBodyFatFormula) {
                    strings.bodyFatFormula
                } else {
                    strings.mifflinFormula
                }
                MiniBadge("${strings.bmrFormula}: $bmrRecipe", color = GymGreen)
                profile.bodyFatPercent?.let {
                    MiniBadge("${strings.bodyFat} ${CalorieCoreFormatter.percent(it)}", color = WarningRed)
                }
            }
        }
    }
}
