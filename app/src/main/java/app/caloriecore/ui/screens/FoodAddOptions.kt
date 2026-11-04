package app.caloriecore.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.caloriecore.R
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing

internal enum class FoodAddMethod { Custom, Barcode, Image, Search }

@Composable
internal fun FoodAddOptions(
    selected: FoodAddMethod?,
    strings: CalorieCoreStrings,
    onSelect: (FoodAddMethod) -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        FoodAddMethod.entries.forEach { method ->
            val picked = method == selected
            val label = when (method) {
                FoodAddMethod.Custom -> strings.customFood
                FoodAddMethod.Barcode -> strings.foodBarcode
                FoodAddMethod.Image -> strings.foodPhoto
                FoodAddMethod.Search -> strings.foodSearch
            }
            val icon = when (method) {
                FoodAddMethod.Custom -> R.drawable.ic_food_custom
                FoodAddMethod.Barcode -> R.drawable.ic_food_barcode
                FoodAddMethod.Image -> R.drawable.ic_food_photo
                FoodAddMethod.Search -> R.drawable.ic_food_search
            }
            val tileColor = when (method) {
                FoodAddMethod.Custom -> Color(0xFFE7C5F0)
                FoodAddMethod.Barcode -> Color(0xFFB9EDC8)
                FoodAddMethod.Image -> Color(0xFFA6D1D0)
                FoodAddMethod.Search -> Color(0xFFFFC6B5)
            }
            val iconColor = Color(0xFF27353B)
            Column(
                modifier = Modifier.weight(1f).clip(MaterialTheme.shapes.small)
                    .selectable(selected = picked, role = Role.Tab, onClick = { onSelect(method) })
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = tileColor,
                    border = if (picked) BorderStroke(2.dp, iconColor) else null
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = when (method) {
                                FoodAddMethod.Barcode -> strings.barcode
                                FoodAddMethod.Image -> strings.imageRecognition
                                else -> null
                            },
                            modifier = Modifier.size(26.dp),
                            tint = iconColor
                        )
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (picked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
