package app.caloriecore.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BurnEstimate
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
internal fun BurnBreakdownCard(
    burn: BurnEstimate,
    strings: CalorieCoreStrings,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    LogCard(contentPadding = PaddingValues(4.dp)) {
        Column {
            Surface(onClick = onToggle, color = Color.Transparent, shape = MaterialTheme.shapes.medium) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.breakdown,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (expanded) strings.hideDetails else strings.showDetails,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    BurnLineItem(strings.baseMetabolism, burn.bmr)
                    BurnLineItem(strings.watchActivity, burn.activeCalories)
                    BurnLineItem(strings.plannedWorkout, burn.plannedTraining)
                    BurnLineItem(strings.loggedActivity, burn.loggedActivityCalories)
                    BurnLineItem("TEF", burn.tef)
                    BurnLineItem(strings.sleepCorrection, burn.sleepAdjustment)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(strings.dailyBurn, style = MaterialTheme.typography.titleSmall)
                        Text(CalorieCoreFormatter.kcal(burn.total), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun BurnLineItem(label: String, kcalDelta: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${if (kcalDelta > 0) "+" else ""}${CalorieCoreFormatter.kcal(kcalDelta)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
