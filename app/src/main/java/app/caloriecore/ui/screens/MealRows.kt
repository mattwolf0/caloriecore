package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MiniBadge
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.logMomentText
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber
import app.caloriecore.ui.theme.GymGreen
import app.caloriecore.ui.theme.WarningRed

internal fun LazyListScope.mealRows(
    meals: List<FoodEntry>,
    strings: CalorieCoreStrings,
    onEdit: (FoodEntry) -> Unit,
    onRemove: (FoodEntry) -> Unit
) {
    item { ShelfHeader(strings.todaysMeals, "${meals.size} ${strings.items}") }
    if (meals.isEmpty()) {
        item { EmptyMealCard(strings.noEntries) }
    } else {
        items(meals, key = { it.id }) { entry ->
            MealLogRow(
                entry = entry,
                strings = strings,
                onEdit = { onEdit(entry) },
                onRemove = { onRemove(entry) }
            )
        }
    }
}

@Composable
private fun MealLogRow(entry: FoodEntry, strings: CalorieCoreStrings, onEdit: () -> Unit, onRemove: () -> Unit) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = entry.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${logMomentText(entry.loggedAt)} | ${entry.servingGrams} g | ${CalorieCoreFormatter.kcal(entry.calories)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    TextButton(onClick = onEdit) { Text(strings.edit) }
                    TextButton(onClick = onRemove) { Text(strings.delete) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                MiniBadge("P ${CalorieCoreFormatter.grams(entry.proteinGrams)}", color = GymGreen)
                MiniBadge("C ${CalorieCoreFormatter.grams(entry.carbGrams)}", color = FoodAmber)
                MiniBadge("F ${CalorieCoreFormatter.grams(entry.fatGrams)}", color = WarningRed)
            }
            if (entry.barcode.isNotBlank()) {
                Text(
                    text = "${strings.barcode}: ${entry.barcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyMealCard(message: String) {
    LogCard {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
