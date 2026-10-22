package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.StatCell
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.NutritionTotals
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
internal fun FoodMacroSummary(nutrition: NutritionTotals, hasMissingMacros: Boolean, strings: CalorieCoreStrings) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text(
                    text = strings.intake,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Text(
                        text = nutrition.calories.toString(),
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " kcal",
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCell(strings.protein, CalorieCoreFormatter.grams(nutrition.protein), MaterialTheme.colorScheme.primary)
                StatCell(strings.carbs, CalorieCoreFormatter.grams(nutrition.carbs), MaterialTheme.colorScheme.secondary)
                StatCell(strings.fat, CalorieCoreFormatter.grams(nutrition.fat), MaterialTheme.colorScheme.tertiary)
            }
            if (hasMissingMacros) {
                Text(
                    text = strings.missingMacroData,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
