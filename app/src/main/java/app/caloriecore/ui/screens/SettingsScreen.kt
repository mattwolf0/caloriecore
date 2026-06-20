package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.StatTile
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.pickedDayReport
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.GymGreen
import app.caloriecore.ui.theme.WarningRed

@Composable
fun SettingsScreen(
    logbook: Logbook,
    strings: CalorieCoreStrings,
    onSettingsChange: (UserPreferences) -> Unit,
    onReset: () -> Unit
) {
    val settingsDay = logbook.pickedDayReport()

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { ScreenName(title = strings.settingsTitle) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(
                    label = strings.profileData,
                    metricText = CalorieCoreFormatter.kilograms(logbook.profile.weightKg),
                    detail = "${logbook.profile.heightCm} cm, ${strings.bmi} " +
                        CalorieCoreFormatter.logDecimal(settingsDay.burnEstimate.bmi),
                    modifier = Modifier.weight(1f),
                    accent = GymGreen
                )
                StatTile(
                    label = strings.restingHeartRate,
                    metricText = logbook.profile.restingHeartRate.toString(),
                    detail = CalorieCoreFormatter.bpm(logbook.profile.restingHeartRate),
                    modifier = Modifier.weight(1f),
                    accent = WarningRed
                )
            }
        }
        item {
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShelfHeader(strings.language)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UiLanguage.entries.forEach { language ->
                            FilterChip(
                                selected = logbook.settings.language == language,
                                onClick = { onSettingsChange(logbook.settings.copy(language = language)) },
                                label = { Text(strings.languageDisplayName(language)) }
                            )
                        }
                    }
                }
            }
        }
        item {
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShelfHeader(strings.theme)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UiThemeMode.entries.forEach { theme ->
                            FilterChip(
                                selected = logbook.settings.themeMode == theme,
                                onClick = { onSettingsChange(logbook.settings.copy(themeMode = theme)) },
                                label = { Text(strings.themeDisplayName(theme)) }
                            )
                        }
                    }
                }
            }
        }
        item {
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShelfHeader(strings.data)
                    Text(
                        text = strings.privacyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SolidActionButton(text = strings.resetData, onClick = onReset)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(54.dp)) }
    }
}
