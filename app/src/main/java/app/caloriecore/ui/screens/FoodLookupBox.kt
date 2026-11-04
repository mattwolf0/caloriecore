package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.FoodProduct
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing

@Composable
internal fun FoodLookupBox(
    editor: FoodEditorState,
    strings: CalorieCoreStrings,
    lookupBusy: Boolean,
    useBarcode: Boolean,
    onScanBarcode: () -> Unit,
    onLookupBarcode: () -> Unit,
    onSearchFood: () -> Unit,
    onSelectProduct: (FoodProduct) -> Unit
) {
    val draft = editor.draft

    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
        if (useBarcode) {
            SolidActionButton(strings.scanBarcode, onScanBarcode, enabled = !lookupBusy)
            OutlinedTextField(
                value = draft.barcode,
                onValueChange = editor::updateBarcode,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.barcode) },
                shape = MaterialTheme.shapes.small,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            SolidActionButton(
                text = if (lookupBusy) strings.lookingUp else strings.findProductByBarcode,
                enabled = draft.barcode.isNotBlank() && !lookupBusy,
                onClick = onLookupBarcode
            )
        } else {
            OutlinedTextField(
                value = draft.offSearchText,
                onValueChange = editor::updateSearchText,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                label = { Text(strings.foodSearchText) },
                singleLine = true
            )
            SolidActionButton(
                text = if (lookupBusy) strings.lookingUp else strings.searchFood,
                enabled = draft.offSearchText.length >= 2 && !lookupBusy,
                onClick = onSearchFood
            )
        }
        if (!useBarcode && editor.offShelf.isNotEmpty()) {
            ShelfHeader(strings.foodSearchResults)
            editor.offShelf.forEach { offFood ->
                OffShelfRow(
                    offFood = offFood,
                    strings = strings,
                    onSelect = { onSelectProduct(offFood) }
                )
            }
        } else if (!useBarcode && editor.hasSearchedOff && editor.lookupNote == null && !lookupBusy) {
            Text(
                text = strings.noFoodSearchResults,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (lookupBusy) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        editor.lookupNote?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OffShelfRow(offFood: FoodProduct, strings: CalorieCoreStrings, onSelect: () -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = UiSpacing.content),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = offFood.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${offFood.kcalPer100g.toKcalText()} / 100 g | " +
                        "P ${offFood.proteinPer100g.toGramText()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onSelect) { Text(strings.selectProduct) }
        }
    }
}

private fun Int?.toKcalText(): String = this?.let(CalorieCoreFormatter::kcal) ?: "— kcal"

private fun Double?.toGramText(): String = this?.let(CalorieCoreFormatter::grams) ?: "—"
