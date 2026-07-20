package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MiniBadge
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BurnEstimate
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber

@Composable
internal fun DailyBurnCard(
    burn: BurnEstimate,
    intakeCalories: Int,
    strings: CalorieCoreStrings
) {
    val balance = intakeCalories - burn.total
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = strings.dailyBalance,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = CalorieCoreFormatter.kcal(balance),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                MiniBadge("${strings.intake}: ${CalorieCoreFormatter.kcal(intakeCalories)}")
                MiniBadge("${strings.dailyBurn}: ${CalorieCoreFormatter.kcal(burn.total)}", color = FoodAmber)
            }
        }
    }
}
