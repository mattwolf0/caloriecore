package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MacroMeter
import app.caloriecore.ui.components.StatTile
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.NutritionTotals
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber
import app.caloriecore.ui.theme.GymGreen
import app.caloriecore.ui.theme.WarningRed

@Composable
internal fun FoodMacroSummary(nutrition: NutritionTotals, strings: CalorieCoreStrings) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                strings.calories,
                nutrition.calories.toString(),
                strings.logged,
                Modifier.weight(1f),
                FoodAmber
            )
            StatTile(
                strings.protein,
                CalorieCoreFormatter.grams(nutrition.protein),
                "${strings.macroTarget}: 170 g",
                Modifier.weight(1f),
                GymGreen
            )
        }
        LogCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShelfHeader(strings.dailyMacros)
                MacroMeter(strings.protein, nutrition.protein, 170.0, GymGreen)
                MacroMeter(strings.carbs, nutrition.carbs, 260.0, FoodAmber)
                MacroMeter(strings.fat, nutrition.fat, 80.0, WarningRed)
            }
        }
    }
}
