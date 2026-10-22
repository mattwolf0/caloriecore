package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogMomentPicker
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.SolidActionButton
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
    onSaveFoodEntry: (FoodEntry) -> Unit,
    onClose: () -> Unit
) {
    val draft = editor.draft

    LogMomentPicker(
        pickedMillis = draft.mealLoggedAt,
        onValueChange = editor::changeLoggedAt,
        dateLabel = strings.date,
        timeLabel = strings.time,
        todayText = strings.todayButton,
        previousDayText = strings.previousDay,
        nextDayText = strings.nextDay
    )
    FoodLookupBox(
        editor = editor,
        strings = strings,
        lookupBusy = lookupBusy,
        onScanBarcode = onScanBarcode,
        onLookupBarcode = onLookupBarcode,
        onSearchFood = onSearchFood,
        onSelectProduct = { editor.useLookupProduct(it, strings.productLoaded) }
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    OutlinedTextField(
        value = draft.name,
        onValueChange = editor::updateFoodName,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(strings.foodName) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        NumberInput(
            label = strings.fat,
            numberText = draft.fat,
            onValueChange = editor::updateFat,
            modifier = Modifier.weight(1f),
            suffix = "g",
            allowDecimal = true
        )
    }
    if (draft.name.isNotBlank() && draft.kcal.isNotBlank() && !editor.canSave) {
        Text(strings.profileSetupInvalid, color = MaterialTheme.colorScheme.error)
    }
    SolidActionButton(
        text = if (editor.isEditing) strings.updateMeal else strings.addMeal,
        enabled = editor.canSave,
        onClick = {
            onSaveFoodEntry(editor.buildEntry())
            editor.clear(selectedDateTime)
            editor.lookupNote = strings.saved
            onClose()
        }
    )
}
