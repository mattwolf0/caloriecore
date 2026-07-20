package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.DayPicker
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.components.StatTile
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.pickedDayReport
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber
import app.caloriecore.ui.theme.GymGreen
import app.caloriecore.ui.theme.WarningRed

@Composable
fun TodayScreen(
    logbook: Logbook,
    strings: CalorieCoreStrings,
    onSelectedDateTimeChange: (Long) -> Unit,
    onProfileChange: (BodySnapshot) -> Unit
) {
    val pickedDayReport = logbook.pickedDayReport()
    val burnMath = pickedDayReport.burnEstimate
    val mealMath = pickedDayReport.nutrition
    val bodyCard = logbook.profile
    var showBurnDetails by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { ScreenName(title = strings.todayTitle) }
        item {
            LogCard {
                DayPicker(
                    pickedMillis = logbook.selectedDateTime,
                    onValueChange = onSelectedDateTimeChange,
                    dateLabel = strings.date,
                    todayText = strings.todayButton,
                    previousDayText = strings.previousDay,
                    nextDayText = strings.nextDay
                )
            }
        }
        item {
            DailyBurnCard(
                burn = burnMath,
                intakeCalories = mealMath.calories,
                strings = strings
            )
        }
        item { ShelfHeader(strings.profileData, strings.saved) }
        item { BodyMetricsEditor(profile = bodyCard, strings = strings, onProfileChange = onProfileChange) }
        item {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showBurnDetails = !showBurnDetails }
            ) {
                Text(if (showBurnDetails) strings.hideDetails else strings.showDetails)
            }
        }
        if (showBurnDetails) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        label = strings.bmr,
                        metricText = burnMath.bmr.toString(),
                        detail = strings.baseMetabolism,
                        modifier = Modifier.weight(1f),
                        accent = GymGreen
                    )
                    StatTile(
                        label = strings.watch,
                        metricText = burnMath.activeCalories.toString(),
                        detail = "${bodyCard.steps} ${strings.steps.lowercase()}",
                        modifier = Modifier.weight(1f),
                        accent = FoodAmber
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        label = strings.training,
                        metricText = burnMath.plannedTraining.toString(),
                        detail = strings.plannedWorkout,
                        modifier = Modifier.weight(1f),
                        accent = WarningRed
                    )
                    StatTile(
                        label = strings.intake,
                        metricText = mealMath.calories.toString(),
                        detail = strings.food,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item { BurnBreakdownCard(burn = burnMath, strings = strings) }
        }
    }
}
