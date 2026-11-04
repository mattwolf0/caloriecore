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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.InputRow
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MacroMeter
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.MacroTarget
import app.caloriecore.ui.model.NutritionTotals
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing

@Composable
internal fun FoodMacroSummary(
    nutrition: NutritionTotals,
    target: MacroTarget,
    hasMissingMacros: Boolean,
    strings: CalorieCoreStrings
) {
    val proteinColor = MaterialTheme.colorScheme.primary
    val carbColor = MaterialTheme.colorScheme.secondary
    val fatColor = MaterialTheme.colorScheme.tertiary
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
            ShelfHeader(
                title = strings.dailyMacros,
                trailing = "${strings.macroTarget}: ${strings.goalDisplayName(target.goal)}"
            )
            CalorieTargetMeter(
                calories = nutrition.calories,
                target = target.calories,
                color = MaterialTheme.colorScheme.primary,
                strings = strings
            )
            InputRow {
                MacroMeter(
                    label = strings.protein,
                    grams = nutrition.protein,
                    target = target.proteinGrams.toDouble(),
                    color = proteinColor,
                    modifier = Modifier.weight(1f)
                )
                MacroMeter(
                    label = strings.carbs,
                    grams = nutrition.carbs,
                    target = target.carbsGrams.toDouble(),
                    color = carbColor,
                    modifier = Modifier.weight(1f)
                )
                MacroMeter(
                    label = strings.fat,
                    grams = nutrition.fat,
                    target = target.fatGrams.toDouble(),
                    color = fatColor,
                    modifier = Modifier.weight(1f)
                )
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

@Composable
private fun CalorieTargetMeter(calories: Int, target: Int, color: Color, strings: CalorieCoreStrings) {
    val progress = if (target > 0) calories.toFloat() / target else 0f
    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(strings.calories, style = MaterialTheme.typography.labelLarge, color = color)
            Text(
                text = "${CalorieCoreFormatter.kcal(calories)} / ${CalorieCoreFormatter.kcal(target)}",
                style = MaterialTheme.typography.labelMedium
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(MaterialTheme.shapes.small),
            color = color,
            trackColor = color.copy(alpha = 0.12f)
        )
    }
}
