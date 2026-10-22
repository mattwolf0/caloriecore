package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.DayPicker
import app.caloriecore.ui.components.PagePadding
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.StatTile
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.text.CalorieCoreStrings
import kotlin.math.roundToInt

private const val WeekWindowDays = 7

@Composable
fun ProgressScreen(logbook: Logbook, strings: CalorieCoreStrings, onSelectedDateTimeChange: (Long) -> Unit) {
    val chartWeek = remember(
        logbook.selectedDateTime,
        logbook.bodyHistory,
        logbook.foodEntries,
        logbook.trainingSessions,
        logbook.activityEntries
    ) {
        weekAroundPickedDay(logbook, WeekWindowDays)
    }
    val pickedDay = chartWeek.last()
    val savedWeights = chartWeek.mapNotNull { it.weightKg }
    val weightChange = pickedDay.weightKg?.takeIf { savedWeights.size > 1 }?.let {
        it - savedWeights.first()
    }
    val savedBalances = chartWeek.mapNotNull { it.balanceKcal }
    val averageBalance = savedBalances.takeIf { it.isNotEmpty() }?.average()?.roundToInt()
    val savedVolumes = chartWeek.mapNotNull { it.trainingVolumeKg }
    val sevenDayTrainingVolume = savedVolumes.takeIf { it.isNotEmpty() }?.sum()?.roundToInt()

    LazyColumn(
        contentPadding = PagePadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenName(title = strings.progressTitle) }
        item {
            DayPicker(
                pickedMillis = logbook.selectedDateTime,
                onValueChange = onSelectedDateTimeChange,
                dateLabel = strings.date,
                todayText = strings.todayButton,
                previousDayText = strings.previousDay,
                nextDayText = strings.nextDay
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    label = strings.bodyWeight,
                    metricText = pickedDay.weightKg?.let(CalorieCoreFormatter::kilograms) ?: "—",
                    detail = weightChange?.let {
                        "${strings.change}: ${CalorieCoreFormatter.signedDelta(it, "kg")}"
                    } ?: strings.noEntries,
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.primary
                )
                StatTile(
                    label = strings.calorieTrend,
                    metricText = averageBalance?.let {
                        CalorieCoreFormatter.signedDelta(it.toDouble(), "kcal")
                    } ?: "—",
                    detail = if (averageBalance == null) strings.noEntries else strings.average,
                    modifier = Modifier.weight(1f),
                    accent = if (averageBalance == null || averageBalance <= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    label = strings.trainingTrend,
                    metricText = sevenDayTrainingVolume?.let(CalorieCoreFormatter::kilograms) ?: "—",
                    detail = if (sevenDayTrainingVolume == null) strings.noEntries else strings.trendWindow,
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.tertiary
                )
                StatTile(
                    label = strings.intake,
                    metricText = pickedDay.intakeKcal?.let(CalorieCoreFormatter::kcal) ?: "—",
                    detail = pickedDay.burnKcal?.let {
                        "${strings.dailyBurn}: ${CalorieCoreFormatter.kcal(it)}"
                    } ?: strings.noEntries,
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.secondary
                )
            }
        }
        item {
            WeightLineCard(
                title = strings.weightTrend,
                trailing = pickedDay.weightKg?.let(CalorieCoreFormatter::kilograms) ?: "—",
                week = chartWeek,
                emptyText = strings.noEntries
            )
        }
        item {
            BalanceBarsCard(
                title = strings.calorieTrend,
                trailing = averageBalance?.let {
                    CalorieCoreFormatter.signedDelta(it.toDouble(), "kcal")
                } ?: "—",
                week = chartWeek,
                emptyText = strings.noEntries
            )
        }
        item {
            VolumeBarsCard(
                title = strings.trainingTrend,
                trailing = sevenDayTrainingVolume?.let(CalorieCoreFormatter::kilograms) ?: "—",
                week = chartWeek,
                emptyText = strings.noEntries
            )
        }
    }
}
