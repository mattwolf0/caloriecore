package app.caloriecore.ui.screens

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import app.caloriecore.ui.components.EmptyCard
import app.caloriecore.ui.components.LogRow
import app.caloriecore.ui.components.RowsCard
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.logTimeText
import app.caloriecore.ui.text.CalorieCoreStrings

internal fun LazyListScope.mealRows(
    meals: List<FoodEntry>,
    strings: CalorieCoreStrings,
    onEdit: (FoodEntry) -> Unit,
    onRemove: (FoodEntry) -> Unit
) {
    item { ShelfHeader(strings.todaysMeals, "${meals.size} ${strings.items}") }
    if (meals.isEmpty()) {
        item { EmptyCard(strings.noEntries) }
    } else {
        item {
            RowsCard(meals) { entry ->
                val protein = MaterialTheme.colorScheme.primary
                val carbs = MaterialTheme.colorScheme.secondary
                val fat = MaterialTheme.colorScheme.tertiary
                LogRow(
                    title = entry.name,
                    subtitle = "${logTimeText(entry.loggedAt)} · ${entry.servingGrams} g",
                    trailingText = CalorieCoreFormatter.kcal(entry.calories),
                    onClick = { onEdit(entry) },
                    onDelete = { onRemove(entry) },
                    deleteLabel = strings.delete,
                    extra = {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = protein)) { append("P ") }
                                append(macroValue(entry.proteinGrams, entry.proteinKnown))
                                withStyle(SpanStyle(color = carbs)) { append("   C ") }
                                append(macroValue(entry.carbGrams, entry.carbsKnown))
                                withStyle(SpanStyle(color = fat)) { append("   F ") }
                                append(macroValue(entry.fatGrams, entry.fatKnown))
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}

private fun macroValue(value: Double, isKnown: Boolean): String = if (isKnown) CalorieCoreFormatter.grams(value) else "—"
