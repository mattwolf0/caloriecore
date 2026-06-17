package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.FoodProduct
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
internal fun FoodLookupBox(
    editor: FoodEditorState,
    strings: CalorieCoreStrings,
    lookupBusy: Boolean,
    onScanBarcode: () -> Unit,
    onLookupBarcode: () -> Unit,
    onSearchFood: () -> Unit,
    onSelectProduct: (FoodProduct) -> Unit
) {
    val draft = editor.draft

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SolidActionButton(
                text = strings.scanBarcode,
                enabled = !lookupBusy,
                modifier = Modifier.weight(1f),
                onClick = onScanBarcode
            )
            SolidActionButton(
                text = if (lookupBusy) strings.lookingUp else strings.findProductByBarcode,
                enabled = draft.barcode.isNotBlank() && !lookupBusy,
                modifier = Modifier.weight(1f),
                onClick = onLookupBarcode
            )
        }
        OutlinedTextField(
            value = draft.barcode,
            onValueChange = editor::updateBarcode,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(strings.barcode) },
            singleLine = true
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft.offSearchText,
                onValueChange = editor::updateSearchText,
                modifier = Modifier.weight(1f),
                label = { Text(strings.foodSearchText) },
                singleLine = true
            )
            TextButton(
                enabled = draft.offSearchText.length >= 2 && !lookupBusy,
                onClick = onSearchFood
            ) {
                Text(strings.searchFood)
            }
        }
        if (editor.offShelf.isNotEmpty()) {
            ShelfHeader(strings.foodSearchResults)
            editor.offShelf.forEach { offFood ->
                OffShelfRow(
                    offFood = offFood,
                    strings = strings,
                    onSelect = { onSelectProduct(offFood) }
                )
            }
        } else if (editor.hasSearchedOff && editor.lookupNote == null) {
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
    LogCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = offFood.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "${CalorieCoreFormatter.kcal(offFood.kcalPer100g)} / 100 g | " +
                        "P ${CalorieCoreFormatter.grams(offFood.proteinPer100g)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onSelect) { Text(strings.selectProduct) }
        }
    }
}
