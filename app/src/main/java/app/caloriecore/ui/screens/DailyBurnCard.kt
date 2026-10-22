package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.SmallStat
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.BurnEstimate
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
internal fun DailyBurnCard(burn: BurnEstimate, intakeCalories: Int, strings: CalorieCoreStrings) {
    val balance = intakeCalories - burn.total
    val intakeShare = if (burn.total > 0) (intakeCalories.toFloat() / burn.total).coerceIn(0f, 1f) else 0f
    val overBurn = balance > 0
    val intakeColor = if (overBurn) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary

    LogCard(contentPadding = PaddingValues(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column {
                Text(
                    text = strings.dailyBalance,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Text(
                        text = CalorieCoreFormatter.signedDelta(balance.toDouble(), "").trim(),
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " kcal",
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LinearProgressIndicator(
                progress = { intakeShare },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = intakeColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SmallStat(
                    label = strings.intake,
                    value = CalorieCoreFormatter.kcal(intakeCalories),
                    accent = intakeColor,
                    modifier = Modifier.weight(1f)
                )
                SmallStat(
                    label = strings.dailyBurn,
                    value = CalorieCoreFormatter.kcal(burn.total),
                    accent = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
