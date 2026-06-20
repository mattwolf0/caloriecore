package app.caloriecore.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.theme.FoodAmber
import app.caloriecore.ui.theme.GymGreen
import app.caloriecore.ui.theme.WarningRed
import kotlin.math.abs

@Composable
internal fun WeightLineCard(
    title: String,
    trailing: String,
    week: List<WeekLedgerPoint>
) {
    val color = GymGreen
    val values = week.map { it.weightKg }
    val minValue = values.minOrNull() ?: 0.0
    val maxValue = values.maxOrNull() ?: 0.0

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShelfHeader(title, trailing)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
            ) {
                val range = (maxValue - minValue).takeIf { it > 0.001 } ?: 1.0
                val stepX = if (week.size > 1) size.width / (week.size - 1) else size.width
                val path = Path()
                repeat(3) { index ->
                    val y = size.height * index / 2f
                    drawLine(
                        color = color.copy(alpha = 0.10f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                week.forEachIndexed { index, day ->
                    val x = stepX * index
                    val y = size.height - (((day.weightKg - minValue) / range).toFloat() * size.height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                week.forEachIndexed { index, day ->
                    val x = stepX * index
                    val y = size.height - (((day.weightKg - minValue) / range).toFloat() * size.height)
                    drawCircle(color = color, radius = 3.2.dp.toPx(), center = Offset(x, y))
                }
            }
            ChartFootnoteRow(
                start = week.first().label,
                end = week.last().label,
                min = CalorieCoreFormatter.kilograms(minValue),
                max = CalorieCoreFormatter.kilograms(maxValue)
            )
        }
    }
}

@Composable
internal fun BalanceBarsCard(
    title: String,
    trailing: String,
    week: List<WeekLedgerPoint>
) {
    val outlineColor = MaterialTheme.colorScheme.outline

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShelfHeader(title, trailing)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
            ) {
                val maxAbs = week.maxOfOrNull { abs(it.balanceKcal) }?.takeIf { it > 0 } ?: 1
                val centerY = size.height * 0.52f
                val slot = size.width / week.size
                val barWidth = slot * 0.46f
                drawLine(
                    color = outlineColor.copy(alpha = 0.35f),
                    start = Offset(0f, centerY),
                    end = Offset(size.width, centerY),
                    strokeWidth = 1.dp.toPx()
                )
                week.forEachIndexed { index, day ->
                    val magnitude = abs(day.balanceKcal).toFloat() / maxAbs.toFloat()
                    val barHeight = magnitude * (size.height * 0.45f)
                    val x = index * slot + (slot - barWidth) / 2f
                    val top = if (day.balanceKcal >= 0) centerY - barHeight else centerY
                    val color = if (day.balanceKcal >= 0) FoodAmber else GymGreen
                    drawRect(
                        color = color,
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight.coerceAtLeast(2.dp.toPx()))
                    )
                }
            }
            ChartFootnoteRow(
                start = week.first().label,
                end = week.last().label,
                min = CalorieCoreFormatter.signedDelta(week.minOf { it.balanceKcal }.toDouble(), "kcal"),
                max = CalorieCoreFormatter.signedDelta(week.maxOf { it.balanceKcal }.toDouble(), "kcal")
            )
        }
    }
}

@Composable
internal fun VolumeBarsCard(
    title: String,
    trailing: String,
    week: List<WeekLedgerPoint>
) {
    val color = WarningRed
    val values = week.map { it.trainingVolumeKg }
    val maxValue = values.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShelfHeader(title, trailing)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
            ) {
                val slot = size.width / week.size
                val barWidth = slot * 0.50f
                repeat(3) { index ->
                    val y = size.height * index / 2f
                    drawLine(
                        color = color.copy(alpha = 0.10f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                week.forEachIndexed { index, day ->
                    val barHeight = ((day.trainingVolumeKg / maxValue).toFloat() * size.height).coerceAtLeast(2.dp.toPx())
                    val x = index * slot + (slot - barWidth) / 2f
                    drawRect(
                        color = color,
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight)
                    )
                }
            }
            ChartFootnoteRow(
                start = week.first().label,
                end = week.last().label,
                min = CalorieCoreFormatter.kilograms(0.0),
                max = CalorieCoreFormatter.kilograms(maxValue)
            )
        }
    }
}

@Composable
private fun ChartFootnoteRow(start: String, end: String, min: String, max: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = start,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$min - $max",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = end,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
