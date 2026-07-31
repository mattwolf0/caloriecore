package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.ClickableLogCard
import app.caloriecore.ui.components.ConfirmActionDialog
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.components.StatTile
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.UserPreferences
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
    var isLanguageDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isThemeDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isResetDialogOpen by rememberSaveable { mutableStateOf(false) }
    var showLegalInfo by rememberSaveable { mutableStateOf(false) }

    if (showLegalInfo) {
        LegalInfoScreen(
            strings = strings,
            onBack = { showLegalInfo = false }
        )
        return
    }

    if (isLanguageDialogOpen) {
        LanguagePickerDialog(
            selectedLanguage = logbook.settings.language,
            strings = strings,
            onLanguageSelected = { language ->
                onSettingsChange(logbook.settings.copy(language = language))
                isLanguageDialogOpen = false
            },
            onDismiss = { isLanguageDialogOpen = false }
        )
    }

    if (isThemeDialogOpen) {
        ThemePickerDialog(
            selectedTheme = logbook.settings.themeMode,
            strings = strings,
            onThemeSelected = { theme ->
                onSettingsChange(logbook.settings.copy(themeMode = theme))
                isThemeDialogOpen = false
            },
            onDismiss = { isThemeDialogOpen = false }
        )
    }

    if (isResetDialogOpen) {
        ConfirmActionDialog(
            title = strings.clearDataConfirmTitle,
            message = strings.clearDataConfirmText,
            confirmText = strings.resetData,
            cancelText = strings.cancel,
            onConfirm = {
                isResetDialogOpen = false
                onReset()
            },
            onDismiss = { isResetDialogOpen = false }
        )
    }

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
            ClickableLogCard(onClick = { isLanguageDialogOpen = true }) {
                SettingsPickerRow(
                    label = strings.language,
                    value = strings.languageDisplayName(logbook.settings.language)
                )
            }
        }
        item {
            ClickableLogCard(onClick = { isThemeDialogOpen = true }) {
                SettingsPickerRow(
                    label = strings.theme,
                    value = strings.themeDisplayName(logbook.settings.themeMode)
                )
            }
        }
        item {
            ClickableLogCard(onClick = { showLegalInfo = true }) {
                SettingsPickerRow(
                    label = strings.legalAndDataSources,
                    value = strings.view
                )
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
                    SolidActionButton(
                        text = strings.resetData,
                        onClick = { isResetDialogOpen = true }
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(54.dp)) }
    }
}

@Composable
private fun SettingsPickerRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$value ›",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ThemePickerDialog(
    selectedTheme: UiThemeMode,
    strings: CalorieCoreStrings,
    onThemeSelected: (UiThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.theme) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                UiThemeMode.entries.forEach { theme ->
                    val isSelected = theme == selectedTheme
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onThemeSelected(theme) }
                            )
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = null)
                        Text(
                            text = strings.themeDisplayName(theme),
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close)
            }
        }
    )
}

@Composable
private fun LanguagePickerDialog(
    selectedLanguage: UiLanguage,
    strings: CalorieCoreStrings,
    onLanguageSelected: (UiLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.language) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                items(UiLanguage.entries) { language ->
                    val isSelected = language == selectedLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onLanguageSelected(language) }
                            )
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = null)
                        Text(
                            text = strings.languageDisplayName(language),
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close)
            }
        }
    )
}
