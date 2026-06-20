package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.LogMomentPicker
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.components.StatTile
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber
import app.caloriecore.ui.theme.GymGreen
import app.caloriecore.ui.theme.WarningRed
import kotlin.math.roundToInt

private const val WeekWindowDays = 7

@Composable
fun ProgressScreen(
    logbook: Logbook,
    strings: CalorieCoreStrings,
    onSelectedDateTimeChange: (Long) -> Unit
) {
    val chartWeek = remember(logbook.selectedDateTime, logbook.bodyHistory, logbook.foodEntries, logbook.trainingSessions) {
        weekAroundPickedDay(logbook, WeekWindowDays)
    }
    val weekStart = chartWeek.first()
    val pickedDay = chartWeek.last()
    val weightChange = pickedDay.weightKg - weekStart.weightKg
    val averageBalance = chartWeek.map { it.balanceKcal }.average().roundToInt()
    val sevenDayTrainingVolume = chartWeek.sumOf { it.trainingVolumeKg }.roundToInt()

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { ScreenName(title = strings.progressTitle) }
        item {
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShelfHeader(strings.selectedMoment, strings.trendWindow)
                    LogMomentPicker(
                        pickedMillis = logbook.selectedDateTime,
                        onValueChange = onSelectedDateTimeChange,
                        dateLabel = strings.date,
                        timeLabel = strings.time,
                        todayText = strings.todayButton
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(
                    label = strings.bodyWeight,
                    metricText = CalorieCoreFormatter.kilograms(pickedDay.weightKg),
                    detail = "${strings.change}: ${CalorieCoreFormatter.signedDelta(weightChange, "kg")}",
                    modifier = Modifier.weight(1f),
                    accent = GymGreen
                )
                StatTile(
                    label = strings.calorieTrend,
                    metricText = CalorieCoreFormatter.signedDelta(averageBalance.toDouble(), "kcal"),
                    detail = strings.average,
                    modifier = Modifier.weight(1f),
                    accent = if (averageBalance <= 0) GymGreen else FoodAmber
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(
                    label = strings.trainingTrend,
                    metricText = CalorieCoreFormatter.kilograms(sevenDayTrainingVolume),
                    detail = strings.trendWindow,
                    modifier = Modifier.weight(1f),
                    accent = WarningRed
                )
                StatTile(
                    label = strings.intake,
                    metricText = CalorieCoreFormatter.kcal(pickedDay.intakeKcal),
                    detail = "${strings.dailyBurn}: ${CalorieCoreFormatter.kcal(pickedDay.burnKcal)}",
                    modifier = Modifier.weight(1f),
                    accent = FoodAmber
                )
            }
        }
        item {
            WeightLineCard(
                title = strings.weightTrend,
                trailing = CalorieCoreFormatter.kilograms(pickedDay.weightKg),
                week = chartWeek
            )
        }
        item {
            BalanceBarsCard(
                title = strings.calorieTrend,
                trailing = CalorieCoreFormatter.signedDelta(averageBalance.toDouble(), "kcal"),
                week = chartWeek
            )
        }
        item {
            VolumeBarsCard(
                title = strings.trainingTrend,
                trailing = CalorieCoreFormatter.kilograms(sevenDayTrainingVolume),
                week = chartWeek
            )
        }
        item { Spacer(modifier = Modifier.height(54.dp)) }
    }
}
