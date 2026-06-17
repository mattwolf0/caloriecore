package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.LogMomentPicker
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
internal fun FoodEntryEditor(
    editor: FoodEditorState,
    strings: CalorieCoreStrings,
    lookupBusy: Boolean,
    selectedDateTime: Long,
    onScanBarcode: () -> Unit,
    onLookupBarcode: () -> Unit,
    onSearchFood: () -> Unit,
    onSaveFoodEntry: (FoodEntry) -> Unit
) {
    val draft = editor.draft

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShelfHeader(if (editor.isEditing) strings.edit else strings.newMeal)
            LogMomentPicker(
                pickedMillis = draft.mealLoggedAt,
                onValueChange = editor::changeLoggedAt,
                dateLabel = strings.date,
                timeLabel = strings.time,
                todayText = strings.todayButton
            )
            FoodLookupBox(
                editor = editor,
                strings = strings,
                lookupBusy = lookupBusy,
                onScanBarcode = onScanBarcode,
                onLookupBarcode = onLookupBarcode,
                onSearchFood = onSearchFood,
                onSelectProduct = { editor.useOffProduct(it, strings.productLoaded) }
            )
            OutlinedTextField(
                value = draft.name,
                onValueChange = editor::updateFoodName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.foodName) },
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInput(
                    strings.serving,
                    draft.grams,
                    editor::updateServing,
                    Modifier.weight(1f),
                    "g"
                )
                NumberInput(
                    label = strings.calories,
                    numberText = draft.kcal,
                    onValueChange = editor::updateCalories,
                    modifier = Modifier.weight(1f),
                    suffix = "kcal"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberInput(
                    label = strings.protein,
                    numberText = draft.protein,
                    onValueChange = editor::updateProtein,
                    modifier = Modifier.weight(1f),
                    suffix = "g",
                    allowDecimal = true
                )
                NumberInput(
                    label = strings.carbs,
                    numberText = draft.carbs,
                    onValueChange = editor::updateCarbs,
                    modifier = Modifier.weight(1f),
                    suffix = "g",
                    allowDecimal = true
                )
            }
            NumberInput(
                label = strings.fat,
                numberText = draft.fat,
                onValueChange = editor::updateFat,
                suffix = "g",
                allowDecimal = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SolidActionButton(
                    text = if (editor.isEditing) strings.updateMeal else strings.addMeal,
                    enabled = editor.canSave,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onSaveFoodEntry(editor.buildEntry())
                        editor.clear(selectedDateTime)
                        editor.lookupNote = strings.saved
                    }
                )
                TextButton(onClick = { editor.clear(selectedDateTime) }) { Text(strings.clear) }
            }
        }
    }
}
