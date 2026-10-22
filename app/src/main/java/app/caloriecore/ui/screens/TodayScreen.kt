package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.DayPicker
import app.caloriecore.ui.components.PagePadding
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.PhoneStepState
import app.caloriecore.ui.model.phoneNowMillis
import app.caloriecore.ui.model.pickedDayReport
import app.caloriecore.ui.model.samePhoneDay
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
fun TodayScreen(
    logbook: Logbook,
    strings: CalorieCoreStrings,
    onSelectedDateTimeChange: (Long) -> Unit,
    onProfileChange: (BodySnapshot) -> Unit,
    phoneStepState: PhoneStepState,
    onPhoneStepsChange: (Boolean) -> Unit,
    onStepPermission: () -> Unit,
    onStepSettings: () -> Unit,
    onStepRetry: () -> Unit,
    saving: Boolean = false,
    saveFailed: Boolean = false
) {
    val pickedDayReport = logbook.pickedDayReport()
    val burnMath = pickedDayReport.burnEstimate
    val mealMath = pickedDayReport.nutrition
    val bodyCard = logbook.profile
    val isToday = samePhoneDay(logbook.selectedDateTime, phoneNowMillis())
    var showBurnDetails by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PagePadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenName(title = strings.todayTitle) }
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
            DailyBurnCard(
                burn = burnMath,
                intakeCalories = mealMath.calories,
                strings = strings
            )
        }
        item {
            BurnBreakdownCard(
                burn = burnMath,
                strings = strings,
                expanded = showBurnDetails,
                onToggle = { showBurnDetails = !showBurnDetails }
            )
        }
        if (isToday) {
            item {
                StepCounterCard(
                    state = phoneStepState,
                    strings = strings,
                    usePhoneSteps = logbook.settings.usePhoneSteps,
                    manualSteps = logbook.profile.steps,
                    onPhoneStepsChange = onPhoneStepsChange,
                    onPermission = onStepPermission,
                    onSettings = onStepSettings,
                    onRetry = onStepRetry
                )
            }
        }
        item {
            ShelfHeader(
                strings.profileData,
                if (saveFailed) {
                    null
                } else if (saving) {
                    strings.saving
                } else {
                    strings.saved
                }
            )
        }
        item {
            BodyMetricsEditor(
                profile = bodyCard,
                strings = strings,
                onProfileChange = onProfileChange,
                phoneStepsOn = logbook.settings.usePhoneSteps && isToday
            )
        }
    }
}
