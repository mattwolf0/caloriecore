package app.caloriecore.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.CalorieCoreBackdrop
import app.caloriecore.ui.model.PhoneStepStatus
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.logDateText
import app.caloriecore.ui.model.phoneNowMillis
import app.caloriecore.ui.model.samePhoneDay
import app.caloriecore.ui.navigation.CalorieCoreTab
import app.caloriecore.ui.screens.FoodScreen
import app.caloriecore.ui.screens.ProfileSetupScreen
import app.caloriecore.ui.screens.ProgressScreen
import app.caloriecore.ui.screens.SettingsScreen
import app.caloriecore.ui.screens.TodayScreen
import app.caloriecore.ui.screens.TrainScreen
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.text.calorieCoreStrings
import app.caloriecore.ui.text.resolveCalorieCoreLanguage
import app.caloriecore.ui.theme.CalorieCoreTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CalorieCoreApp() {
    val context = LocalContext.current.applicationContext
    val appState = rememberAppState(context)
    val tabState = rememberSaveableStateHolder()
    val screenScope = rememberCoroutineScope()
    var savedMoment by rememberSaveable { mutableStateOf<Long?>(null) }
    var savedFollowToday by rememberSaveable { mutableStateOf(true) }
    val phoneSteps = rememberPhoneSteps(
        enabled = appState.logbook?.settings?.let { it.profileSetupComplete && it.usePhoneSteps } == true
    )
    var currentTab by rememberSaveable { mutableStateOf(CalorieCoreTab.Today) }
    var lastStepSaveAt by remember { mutableLongStateOf(0L) }

    LaunchedEffect(appState) {
        appState.load(savedMoment, savedFollowToday)
    }

    LaunchedEffect(appState.logbook?.selectedDateTime, appState.followsToday) {
        appState.logbook?.let {
            savedMoment = it.selectedDateTime
            savedFollowToday = appState.followsToday
        }
    }

    LaunchedEffect(appState) {
        while (true) {
            appState.updateToday()
            delay(15_000L)
        }
    }

    LaunchedEffect(phoneSteps.state, appState.logbook?.selectedDateTime, appState.logbook?.settings?.usePhoneSteps) {
        val logbook = appState.logbook ?: return@LaunchedEffect
        if (
            logbook.settings.usePhoneSteps &&
            phoneSteps.state.status == PhoneStepStatus.Active &&
            phoneSteps.state.day == logDateText(phoneNowMillis()) &&
            samePhoneDay(logbook.selectedDateTime, phoneNowMillis()) &&
            logbook.profile.steps != phoneSteps.state.steps
        ) {
            val saveWait = lastStepSaveAt + StepSaveIntervalMillis - phoneNowMillis()
            if (lastStepSaveAt > 0L && saveWait > 0L) {
                delay(saveWait)
            }

            val currentLogbook = appState.logbook ?: return@LaunchedEffect
            if (
                currentLogbook.settings.usePhoneSteps &&
                phoneSteps.state.status == PhoneStepStatus.Active &&
                phoneSteps.state.day == logDateText(phoneNowMillis()) &&
                samePhoneDay(currentLogbook.selectedDateTime, phoneNowMillis()) &&
                currentLogbook.profile.steps != phoneSteps.state.steps
            ) {
                appState.savePhoneSteps(phoneSteps.state.steps)
                lastStepSaveAt = phoneNowMillis()
            }
        }
    }

    val logbook = appState.logbook
    CalorieCoreTheme(themeMode = logbook?.settings?.themeMode ?: UiThemeMode.System) {
        CalorieCoreBackdrop {
            if (logbook == null) {
                if (appState.loadFailed) {
                    val strings = calorieCoreStrings(UiLanguage.System)
                    Column(modifier = Modifier.statusBarsPadding().padding(16.dp)) {
                        Text(strings.loadFailed)
                        TextButton(onClick = { screenScope.launch { appState.load(savedMoment, savedFollowToday) } }) {
                            Text(strings.tryAgain)
                        }
                    }
                } else {
                    FirstLoadSpinner()
                }
            } else {
                val strings = calorieCoreStrings(logbook.settings.language)
                if (!logbook.settings.profileSetupComplete) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        topBar = { SaveError(appState, strings) }
                    ) { innerPadding ->
                        ProfileSetupScreen(
                            selectedDateTime = logbook.selectedDateTime,
                            strings = strings,
                            onComplete = appState::completeProfileSetup,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        topBar = { SaveError(appState, strings) },
                        bottomBar = {
                            BottomDock(
                                currentTab = currentTab,
                                tabLabel = strings::dockLabel,
                                onSelect = { currentTab = it }
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            tabState.SaveableStateProvider(currentTab.name) {
                                when (currentTab) {
                                    CalorieCoreTab.Today -> TodayScreen(
                                        logbook = logbook,
                                        strings = strings,
                                        onSelectedDateTimeChange = appState::jumpToMoment,
                                        onProfileChange = appState::saveBodyEntry,
                                        phoneStepState = phoneSteps.state,
                                        onPhoneStepsChange = { appState.saveSettings(logbook.settings.copy(usePhoneSteps = it)) },
                                        onStepPermission = phoneSteps.requestPermission,
                                        onStepSettings = phoneSteps.openSettings,
                                        onStepRetry = phoneSteps.retry,
                                        saving = appState.isSaving,
                                        saveFailed = appState.saveFailed
                                    )

                                    CalorieCoreTab.Food -> FoodScreen(
                                        logbook = logbook,
                                        strings = strings,
                                        onSelectedDateTimeChange = appState::jumpToMoment,
                                        onSaveFoodEntry = appState::saveMeal,
                                        onRemoveFoodEntry = appState::deleteMeal
                                    )

                                    CalorieCoreTab.Train -> TrainScreen(
                                        logbook = logbook,
                                        strings = strings,
                                        onSelectedDateTimeChange = appState::jumpToMoment,
                                        activityCatalog = appState.activityCatalog?.items.orEmpty(),
                                        activityLanguage = resolveCalorieCoreLanguage(logbook.settings.language),
                                        onSaveActivity = appState::saveActivity,
                                        onRemoveActivity = appState::deleteActivity,
                                        onSavePlan = appState::savePlan,
                                        onRemovePlan = appState::deletePlan,
                                        onSaveSession = appState::saveWorkout,
                                        onRemoveSession = appState::deleteWorkout
                                    )

                                    CalorieCoreTab.Progress -> ProgressScreen(
                                        logbook = logbook,
                                        strings = strings,
                                        onSelectedDateTimeChange = appState::jumpToMoment
                                    )

                                    CalorieCoreTab.Settings -> SettingsScreen(
                                        logbook = logbook,
                                        strings = strings,
                                        onSettingsChange = appState::saveSettings,
                                        onReset = {
                                            appState.resetLogbook()
                                            CalorieCoreTab.entries.forEach { tabState.removeState(it.name) }
                                            currentTab = CalorieCoreTab.Today
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val StepSaveIntervalMillis = 5 * 60_000L

@Composable
private fun FirstLoadSpinner() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.5.dp
        )
    }
}

@Composable
private fun SaveError(appState: AppState, strings: CalorieCoreStrings) {
    if (appState.saveFailed) {
        Column(modifier = Modifier.statusBarsPadding().padding(12.dp)) {
            Text(strings.saveFailed, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = appState::retrySave) { Text(strings.tryAgain) }
        }
    }
}
