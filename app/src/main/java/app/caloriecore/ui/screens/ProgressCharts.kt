package app.caloriecore.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.InputRow
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.logDateText
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing

internal data class ChartLine(val name: String, val values: List<Double?>, val color: Color)

@Composable
internal fun ProgressChartCard(
    title: String,
    days: List<ProgressDay>,
    lines: List<ChartLine>,
    unit: String,
    strings: CalorieCoreStrings,
    pickedIndex: Int,
    onSelectDay: (Int) -> Unit,
    isLineChart: Boolean = false,
    startAtZero: Boolean = !isLineChart,
    onOpenDay: ((Long) -> Unit)? = null
) {
    val values = remember(lines) { lines.flatMap { it.values }.filterNotNull().filter { it.isFinite() } }
    val range = remember(values, startAtZero) { chartRange(values, startAtZero = startAtZero) }
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val selectedDay = days.getOrNull(pickedIndex)
    val description = remember(title, lines, unit, pickedIndex) {
        title + ". " + lines.joinToString { line ->
            line.name + " " + chartValue(line.values.getOrNull(pickedIndex), unit)
        }
    }
    val selectDay = rememberUpdatedState(onSelectDay)

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
            ShelfHeader(title, unit.ifBlank { null })
            if (values.isEmpty() || days.isEmpty()) {
                Text(strings.chartEmpty, Modifier.padding(vertical = UiSpacing.content),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                if (values.all { it == 0.0 }) {
                    Text(strings.chartZero, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val plotHeight = if (days.size > 14) 132.dp else 116.dp
                Row(horizontalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
                    Column(Modifier.height(plotHeight),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End) {
                        listOf(range.high, (range.high + range.low) / 2, range.low).forEach { tick ->
                            Text(CalorieCoreFormatter.logDecimal(tick),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(UiSpacing.tight)) {
                        Canvas(Modifier.fillMaxWidth().height(plotHeight)
                            .semantics { contentDescription = description }
                            .pointerInput(days.size) {
                                detectTapGestures { tap ->
                                    val picked = (tap.x / size.width * days.size).toInt()
                                        .coerceIn(0, days.lastIndex)
                                    selectDay.value(picked)
                                }
                            }) {
                            val inset = 8.dp.toPx()
                            val plotHeightPx = size.height - inset * 2
                            val slot = size.width / days.size
                            fun x(index: Int) = slot * (index + 0.5f)
                            fun y(value: Double) = inset + plotHeightPx * (1 - (value - range.low) / (range.high - range.low)).toFloat()
                            repeat(3) { index ->
                                val gridY = inset + plotHeightPx * index / 2
                                drawLine(outline, Offset(0f, gridY), Offset(size.width, gridY), 1.dp.toPx())
                            }
                            drawLine(outline, Offset(x(pickedIndex), inset),
                                Offset(x(pickedIndex), size.height - inset), 1.dp.toPx())
                            lines.forEachIndexed { lineIndex, line ->
                                if (isLineChart) {
                                    val path = Path()
                                    var started = false
                                    line.values.forEachIndexed { index, value ->
                                        if (value == null || !value.isFinite()) {
                                            started = false
                                        } else {
                                            if (started) path.lineTo(x(index), y(value)) else path.moveTo(x(index), y(value))
                                            started = true
                                        }
                                    }
                                    drawPath(path, line.color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
                                    line.values.forEachIndexed { index, value ->
                                        if (value != null && value.isFinite()) drawCircle(
                                            line.color, (if (index == pickedIndex) 5 else 3).dp.toPx(), Offset(x(index), y(value)))
                                    }
                                } else {
                                    val barWidth = slot * 0.7f / lines.size
                                    line.values.forEachIndexed { index, value ->
                                        if (value != null && value.isFinite()) {
                                            val left = slot * index + slot * 0.15f + barWidth * lineIndex
                                            val zeroY = y(0.0)
                                            val endY = y(value)
                                            val color = line.color.copy(alpha = if (index == pickedIndex) 1f else 0.6f)
                                            if (value == 0.0) {
                                                drawLine(color, Offset(left, zeroY), Offset(left + barWidth, zeroY), 2.dp.toPx())
                                            } else {
                                                drawRoundRect(color, Offset(left, minOf(zeroY, endY)),
                                                    Size((barWidth - 1.dp.toPx()).coerceAtLeast(1f), kotlin.math.abs(endY - zeroY)), CornerRadius(2.dp.toPx()))
                                            }
                                        }
                                    }
                                }
                            }
                            if (range.low < 0 && range.high > 0) {
                                drawLine(outline, Offset(0f, y(0.0)), Offset(size.width, y(0.0)), 1.dp.toPx())
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf(days.first(), days[days.size / 2], days.last()).forEach { point ->
                                Text(point.label, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                InputRow {
                    TextButton(
                        enabled = pickedIndex > 0, onClick = { onSelectDay(pickedIndex - 1) },
                        modifier = Modifier.weight(1f).semantics { contentDescription = strings.previousDay }
                    ) { Text("‹ ${strings.previousDay}") }
                    TextButton(
                        enabled = pickedIndex < days.lastIndex, onClick = { onSelectDay(pickedIndex + 1) },
                        modifier = Modifier.weight(1f).semantics { contentDescription = strings.nextDay }
                    ) { Text("${strings.nextDay} ›") }
                }
                Text("${strings.selectedDay}: ${selectedDay?.let { logDateText(it.millis) }.orEmpty()}",
                    style = MaterialTheme.typography.labelLarge)
                lines.forEach { line ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
                        Text(line.name, Modifier.weight(1f), color = line.color)
                        Text(chartValue(line.values.getOrNull(pickedIndex), unit), style = MaterialTheme.typography.titleSmall)
                    }
                }
                Text(strings.chartHint, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (onOpenDay != null && selectedDay != null) {
                    TextButton(onClick = { onOpenDay(selectedDay.millis) }, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.openDay)
                    }
                }
            }
        }
    }
}

private fun chartValue(value: Double?, unit: String): String =
    if (value == null || !value.isFinite()) "—" else
        (CalorieCoreFormatter.logDecimal(value) + if (unit.isBlank()) "" else " $unit")
