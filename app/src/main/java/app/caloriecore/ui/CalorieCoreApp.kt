package app.caloriecore.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.caloriecore.data.LogbookRepository
import app.caloriecore.ui.components.CalorieCoreBackdrop
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.navigation.CalorieCoreTab
import app.caloriecore.ui.screens.FoodScreen
import app.caloriecore.ui.screens.ProgressScreen
import app.caloriecore.ui.screens.SettingsScreen
import app.caloriecore.ui.screens.TodayScreen
import app.caloriecore.ui.screens.TrainScreen
import app.caloriecore.ui.text.calorieCoreStrings
import app.caloriecore.ui.theme.CalorieCoreTheme

@Composable
fun CalorieCoreApp() {
    val context = LocalContext.current.applicationContext
    val appState = rememberAppState(
        repository = remember { LogbookRepository(context) }
    )
    var currentTab by remember { mutableStateOf(CalorieCoreTab.Today) }

    LaunchedEffect(appState) {
        appState.load()
    }

    val logbook = appState.logbook
    CalorieCoreTheme(themeMode = logbook?.settings?.themeMode ?: UiThemeMode.System) {
        CalorieCoreBackdrop {
            if (logbook == null) {
                FirstLoadSpinner()
            } else {
                val strings = calorieCoreStrings(logbook.settings.language)
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    bottomBar = {
                        BottomDock(
                            currentTab = currentTab,
                            tabLabel = strings::dockLabel,
                            onSelect = { currentTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            CalorieCoreTab.Today -> TodayScreen(
                                logbook = logbook,
                                strings = strings,
                                onSelectedDateTimeChange = appState::jumpToMoment,
                                onProfileChange = appState::saveBodyEntry
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
                                onReset = appState::resetLogbook
                            )
                        }
                    }
                }
            }
        }
    }
}

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
