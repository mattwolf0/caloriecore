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
import app.caloriecore.ui.model.NutritionTotals
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
internal fun FoodMacroSummary(
    nutrition: NutritionTotals,
    hasMissingMacros: Boolean,
    strings: CalorieCoreStrings
) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShelfHeader(
                title = strings.dailyMacros,
                trailing = CalorieCoreFormatter.kcal(nutrition.calories)
            )
            MacroValue(strings.protein, CalorieCoreFormatter.grams(nutrition.protein))
            MacroValue(strings.carbs, CalorieCoreFormatter.grams(nutrition.carbs))
            MacroValue(strings.fat, CalorieCoreFormatter.grams(nutrition.fat))
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

@Composable
private fun MacroValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
