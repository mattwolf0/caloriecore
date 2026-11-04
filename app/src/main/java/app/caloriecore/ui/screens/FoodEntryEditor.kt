package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.caloriecore.ui.components.EditorActions
import app.caloriecore.ui.components.InputRow
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.LogMomentPicker
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.FoodProduct
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing

@Composable
internal fun FoodEntryEditor(
    editor: FoodEditorState,
    strings: CalorieCoreStrings,
    lookupBusy: Boolean,
    method: FoodAddMethod?,
    onMethodChange: (FoodAddMethod) -> Unit,
    onScanBarcode: () -> Unit,
    onLookupBarcode: () -> Unit,
    onSearchFood: () -> Unit,
    onSelectProduct: (FoodProduct) -> Unit,
    onSaveFoodEntry: (FoodEntry) -> Unit,
    onClose: () -> Unit
) {
    val draft = editor.draft

    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
        ScreenName(if (editor.isEditing) strings.edit else strings.addMeal)
        FoodAddOptions(selected = method, strings = strings, onSelect = onMethodChange)
        if (method != FoodAddMethod.Custom) {
            when (method) {
                FoodAddMethod.Barcode, FoodAddMethod.Search -> LogCard {
                    FoodLookupBox(
                        editor = editor, strings = strings, lookupBusy = lookupBusy,
                        useBarcode = method == FoodAddMethod.Barcode,
                        onScanBarcode = onScanBarcode, onLookupBarcode = onLookupBarcode,
                        onSearchFood = onSearchFood, onSelectProduct = onSelectProduct
                    )
                }
                FoodAddMethod.Image -> LogCard {
                    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
                        Text(strings.comingSoon, style = MaterialTheme.typography.titleMedium)
                        Text(strings.imageFoodSoon, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> Unit
            }
            TextButton(onClick = onClose) { Text(strings.cancel) }
            return@Column
        }
        editor.lookupNote?.let { message ->
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LogCard {
            LogMomentPicker(
                pickedMillis = draft.mealLoggedAt,
                onValueChange = editor::changeLoggedAt,
                dateLabel = strings.date,
                timeLabel = strings.time,
                todayText = strings.todayButton,
                previousDayText = strings.previousDay,
                nextDayText = strings.nextDay
            )
        }
        LogCard {
            Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
                OutlinedTextField(
                    value = draft.name, onValueChange = editor::updateFoodName,
                    modifier = Modifier.fillMaxWidth(), label = { Text(strings.foodName) },
                    shape = MaterialTheme.shapes.small, singleLine = true
                )
                InputRow {
                    NumberInput(
                        label = strings.serving,
                        numberText = draft.grams,
                        onValueChange = editor::updateServing,
                        modifier = Modifier.weight(1f),
                        suffix = "g"
                    )
                    NumberInput(
                        label = strings.calories,
                        numberText = draft.kcal,
                        onValueChange = editor::updateCalories,
                        modifier = Modifier.weight(1f),
                        suffix = "kcal"
                    )
                }
                InputRow {
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
                if (!editor.canSave) {
                    Text(strings.foodSaveHint, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        EditorActions(
            saveText = if (editor.isEditing) strings.updateMeal else strings.addMeal,
            canSave = editor.canSave, strings = strings,
            onSave = { onSaveFoodEntry(editor.buildEntry()) }, onCancel = onClose
        )
    }
}
