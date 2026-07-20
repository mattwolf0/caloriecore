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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    week: List<WeekLedgerPoint>,
    emptyText: String
) {
    val color = GymGreen
    val values = week.mapNotNull { it.weightKg }
    val minValue = values.minOrNull() ?: 0.0
    val maxValue = values.maxOrNull() ?: 0.0
    val chartDescription = "$title. " + week.joinToString { day ->
        "${day.label}: ${day.weightKg?.let(CalorieCoreFormatter::kilograms) ?: emptyText}"
    }

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShelfHeader(title, trailing)
            if (values.isEmpty()) {
                EmptyChartText(emptyText)
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .semantics { contentDescription = chartDescription }
                ) {
                    val range = (maxValue - minValue).takeIf { it > 0.001 } ?: 1.0
                    val stepX = if (week.size > 1) size.width / (week.size - 1) else size.width
                    val path = Path()
                    var lineStarted = false
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
                        val weight = day.weightKg
                        if (weight == null) {
                            lineStarted = false
                        } else {
                            val x = stepX * index
                            val y = size.height - (((weight - minValue) / range).toFloat() * size.height)
                            if (lineStarted) path.lineTo(x, y) else path.moveTo(x, y)
                            lineStarted = true
                        }
                    }
                    drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                    week.forEachIndexed { index, day ->
                        day.weightKg?.let { weight ->
                            val x = stepX * index
                            val y = size.height - (((weight - minValue) / range).toFloat() * size.height)
                            drawCircle(color = color, radius = 3.2.dp.toPx(), center = Offset(x, y))
                        }
                    }
                }
            }
            ChartFootnoteRow(
                start = week.first().label,
                end = week.last().label,
                min = if (values.isEmpty()) "—" else CalorieCoreFormatter.kilograms(minValue),
                max = if (values.isEmpty()) "—" else CalorieCoreFormatter.kilograms(maxValue)
            )
        }
    }
}

@Composable
internal fun BalanceBarsCard(
    title: String,
    trailing: String,
    week: List<WeekLedgerPoint>,
    emptyText: String
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val values = week.mapNotNull { it.balanceKcal }
    val chartDescription = "$title. " + week.joinToString { day ->
        "${day.label}: ${day.balanceKcal?.let { CalorieCoreFormatter.kcal(it) } ?: emptyText}"
    }

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShelfHeader(title, trailing)
            if (values.isEmpty()) {
                EmptyChartText(emptyText)
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .semantics { contentDescription = chartDescription }
                ) {
                    val maxAbs = values.maxOf { abs(it) }.takeIf { it > 0 } ?: 1
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
                        day.balanceKcal?.let { balance ->
                            val magnitude = abs(balance).toFloat() / maxAbs.toFloat()
                            val barHeight = magnitude * (size.height * 0.45f)
                            val x = index * slot + (slot - barWidth) / 2f
                            val top = if (balance >= 0) centerY - barHeight else centerY
                            val color = if (balance >= 0) FoodAmber else GymGreen
                            drawRect(
                                color = color,
                                topLeft = Offset(x, top),
                                size = Size(barWidth, barHeight.coerceAtLeast(2.dp.toPx()))
                            )
                        }
                    }
                }
            }
            ChartFootnoteRow(
                start = week.first().label,
                end = week.last().label,
                min = values.minOrNull()?.let {
                    CalorieCoreFormatter.signedDelta(it.toDouble(), "kcal")
                } ?: "—",
                max = values.maxOrNull()?.let {
                    CalorieCoreFormatter.signedDelta(it.toDouble(), "kcal")
                } ?: "—"
            )
        }
    }
}

@Composable
internal fun VolumeBarsCard(
    title: String,
    trailing: String,
    week: List<WeekLedgerPoint>,
    emptyText: String
) {
    val color = WarningRed
    val values = week.mapNotNull { it.trainingVolumeKg }
    val displayMax = values.maxOrNull() ?: 0.0
    val maxValue = displayMax.takeIf { it > 0.0 } ?: 1.0
    val chartDescription = "$title. " + week.joinToString { day ->
        "${day.label}: ${day.trainingVolumeKg?.let(CalorieCoreFormatter::kilograms) ?: emptyText}"
    }

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShelfHeader(title, trailing)
            if (values.isEmpty()) {
                EmptyChartText(emptyText)
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .semantics { contentDescription = chartDescription }
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
                        day.trainingVolumeKg?.let { volume ->
                            val barHeight = ((volume / maxValue).toFloat() * size.height).coerceAtLeast(2.dp.toPx())
                            val x = index * slot + (slot - barWidth) / 2f
                            drawRect(
                                color = color,
                                topLeft = Offset(x, size.height - barHeight),
                                size = Size(barWidth, barHeight)
                            )
                        }
                    }
                }
            }
            ChartFootnoteRow(
                start = week.first().label,
                end = week.last().label,
                min = if (values.isEmpty()) "—" else CalorieCoreFormatter.kilograms(0.0),
                max = if (values.isEmpty()) "—" else CalorieCoreFormatter.kilograms(displayMax)
            )
        }
    }
}

@Composable
private fun EmptyChartText(message: String) {
    Text(
        text = message,
        modifier = Modifier.height(48.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
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
