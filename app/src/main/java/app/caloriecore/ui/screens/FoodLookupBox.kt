package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.SoftActionButton
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.FoodProduct
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.LineIcons

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
    val canSearch = draft.offSearchText.length >= 2 && !lookupBusy
    val canLookupBarcode = draft.barcode.isNotBlank() && !lookupBusy

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = draft.offSearchText,
            onValueChange = editor::updateSearchText,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(strings.searchFood) },
            trailingIcon = {
                IconButton(enabled = canSearch, onClick = onSearchFood) {
                    Icon(Icons.Rounded.Search, contentDescription = strings.searchFood)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { if (canSearch) onSearchFood() })
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft.barcode,
                onValueChange = editor::updateBarcode,
                modifier = Modifier.weight(1f),
                label = { Text(strings.barcode) },
                trailingIcon = {
                    IconButton(enabled = canLookupBarcode, onClick = onLookupBarcode) {
                        Icon(Icons.Rounded.Search, contentDescription = strings.findProductByBarcode)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { if (canLookupBarcode) onLookupBarcode() })
            )
            SoftActionButton(
                text = strings.scanBarcode,
                icon = LineIcons.Scan,
                enabled = !lookupBusy,
                onClick = onScanBarcode,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp)
            )
        }
        if (lookupBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape))
        }
        editor.lookupNote?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (editor.offShelf.isNotEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column {
                    Text(
                        text = strings.foodSearchResults,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    editor.offShelf.forEachIndexed { index, offFood ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                        OffShelfRow(
                            offFood = offFood,
                            strings = strings,
                            onSelect = { onSelectProduct(offFood) }
                        )
                    }
                }
            }
        } else if (editor.hasSearchedOff && editor.lookupNote == null) {
            Text(
                text = strings.noFoodSearchResults,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OffShelfRow(offFood: FoodProduct, strings: CalorieCoreStrings, onSelect: () -> Unit) {
    Surface(onClick = onSelect, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = offFood.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${offFood.kcalPer100g.toKcalText()} / 100 g · P ${offFood.proteinPer100g.toGramText()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = strings.selectProduct,
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun Int?.toKcalText(): String = this?.let(CalorieCoreFormatter::kcal) ?: "— kcal"

private fun Double?.toGramText(): String = this?.let(CalorieCoreFormatter::grams) ?: "—"
