package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.caloriecore.ui.components.DayPicker
import app.caloriecore.ui.components.InputRow
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MetricValue
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing
import kotlin.math.roundToInt

private enum class ProgressGroup { Food, Body, Activity }

private data class ProgressChart(
    val title: String,
    val lines: List<ChartLine>,
    val unit: String,
    val isLineChart: Boolean = false,
    val startAtZero: Boolean = !isLineChart
)

private fun chartsFor(
    group: ProgressGroup,
    days: List<ProgressDay>,
    strings: CalorieCoreStrings,
    green: Color,
    amber: Color
): List<ProgressChart> = when (group) {
    ProgressGroup.Food -> listOf(
        ProgressChart(
            title = strings.energyTrend,
            lines = listOf(
                ChartLine(strings.intake, days.map { it.intakeKcal?.toDouble() }, green),
                ChartLine(strings.dailyBurn, days.map { it.burnKcal?.toDouble() }, amber)
            ),
            unit = "kcal"
        )
    )
    ProgressGroup.Body -> listOf(
        ProgressChart(
            title = strings.bodyWeight,
            lines = listOf(ChartLine(strings.weight, days.map { it.weightKg }, green)),
            unit = "kg",
            isLineChart = true
        ),
        ProgressChart(
            title = strings.bodyFat,
            lines = listOf(ChartLine(strings.bodyFat, days.map { it.bodyFat }, amber)),
            unit = "%",
            isLineChart = true
        )
    )
    ProgressGroup.Activity -> listOf(
        ProgressChart(
            title = strings.steps,
            lines = listOf(ChartLine(strings.steps, days.map { it.steps?.toDouble() }, green)),
            unit = ""
        ),
        ProgressChart(
            title = strings.trainingTrend,
            lines = listOf(ChartLine(strings.volume, days.map { it.trainingVolumeKg }, green)),
            unit = "kg"
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    logbook: Logbook,
    strings: CalorieCoreStrings,
    onSelectedDateTimeChange: (Long) -> Unit,
    onOpenDay: ((Long) -> Unit)? = null
) {
    var dayCount by rememberSaveable { mutableIntStateOf(7) }
    var group by rememberSaveable { mutableStateOf(ProgressGroup.Food) }
    val progressDays = remember(dayCount, logbook) { daysForProgress(logbook, dayCount) }
    val stats = remember(progressDays) { ProgressStats(progressDays) }
    var pickedIndex by rememberSaveable(dayCount, logbook.selectedDateTime) { mutableIntStateOf(dayCount - 1) }
    val green = MaterialTheme.colorScheme.primary
    val amber = MaterialTheme.colorScheme.secondary
    val charts = remember(group, progressDays, strings, green, amber) {
        chartsFor(group, progressDays, strings, green, amber)
    }

    LazyColumn(contentPadding = PaddingValues(UiSpacing.page), verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
        item { ScreenName(strings.progressTitle) }
        item {
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
                    Text(strings.periodEnding, style = MaterialTheme.typography.bodySmall)
                    DayPicker(
                        pickedMillis = logbook.selectedDateTime,
                        onValueChange = onSelectedDateTimeChange,
                        dateLabel = strings.date,
                        todayText = strings.todayButton,
                        previousDayText = strings.previousDay,
                        nextDayText = strings.nextDay
                    )
                    InputRow {
                        FilterChip(
                            selected = dayCount == 7,
                            onClick = { dayCount = 7 },
                            label = { Text(strings.trendWindow) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = dayCount == 30,
                            onClick = { dayCount = 30 },
                            label = { Text(strings.monthWindow) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(UiSpacing.small),
                verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
                ProgressGroup.entries.forEach { choice ->
                    val label = when (choice) {
                        ProgressGroup.Food -> strings.food
                        ProgressGroup.Body -> strings.bodyStats
                        ProgressGroup.Activity -> strings.activity
                    }
                    FilterChip(
                        selected = group == choice,
                        onClick = { group = choice },
                        label = { Text(label) }
                    )
                }
            }
        }
        item { PeriodSummary(group, stats, dayCount, strings) }
        items(charts, key = { it.title }) { chart ->
            ProgressChartCard(
                title = chart.title, days = progressDays, lines = chart.lines, unit = chart.unit, strings = strings,
                pickedIndex = pickedIndex, onSelectDay = { pickedIndex = it },
                isLineChart = chart.isLineChart, startAtZero = chart.startAtZero, onOpenDay = onOpenDay
            )
        }
    }
}

@Composable
private fun PeriodSummary(group: ProgressGroup, stats: ProgressStats, dayCount: Int, strings: CalorieCoreStrings) {
    val count = when (group) {
        ProgressGroup.Food -> stats.foodDays
        ProgressGroup.Body -> stats.bodyDays
        ProgressGroup.Activity -> stats.stepDays
    }
    val countLabel = when (group) {
        ProgressGroup.Food -> strings.food
        ProgressGroup.Body -> strings.bodyStats
        ProgressGroup.Activity -> strings.steps
    }
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
            ShelfHeader(strings.periodSummary)
            InputRow {
                when (group) {
                    ProgressGroup.Food -> {
                        MetricValue(
                            label = "${strings.average} · ${strings.intake}",
                            value = stats.averageIntake?.let {
                                CalorieCoreFormatter.kcal(it.roundToInt())
                            } ?: "—",
                            modifier = Modifier.weight(1f)
                        )
                        MetricValue(
                            label = "${strings.average} · ${strings.dailyBalance}",
                            value = stats.averageBalance?.let {
                                CalorieCoreFormatter.kcal(it.roundToInt())
                            } ?: "—",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    ProgressGroup.Body -> {
                        MetricValue(
                            label = "${strings.bodyWeight} · ${strings.change}",
                            value = stats.weightChange?.let {
                                CalorieCoreFormatter.signedDelta(it, "kg")
                            } ?: "—",
                            modifier = Modifier.weight(1f)
                        )
                        MetricValue(
                            label = "${strings.average} · ${strings.sleep}",
                            value = stats.averageSleep?.let {
                                "${CalorieCoreFormatter.logDecimal(it)} h"
                            } ?: "—",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    ProgressGroup.Activity -> {
                        MetricValue(
                            label = "${strings.average} · ${strings.steps}",
                            value = stats.averageSteps?.roundToInt()?.toString() ?: "—",
                            modifier = Modifier.weight(1f)
                        )
                        MetricValue(
                            label = strings.workouts,
                            value = stats.workouts.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            if (group == ProgressGroup.Activity) {
                InputRow {
                    MetricValue(
                        label = strings.loggedActivity,
                        value = CalorieCoreFormatter.kcal(stats.activityCalories),
                        modifier = Modifier.weight(1f)
                    )
                    MetricValue(
                        label = strings.sets,
                        value = stats.sets.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Text("$countLabel: $count / $dayCount ${strings.daysLogged}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(strings.progressDataHint, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
