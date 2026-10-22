package app.caloriecore.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.ConfirmActionDialog
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.PagePadding
import app.caloriecore.ui.components.RowsCard
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.SettingsRow
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.components.StatCell
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.model.pickedDayReport
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.LineIcons

@Composable
fun SettingsScreen(logbook: Logbook, strings: CalorieCoreStrings, onSettingsChange: (UserPreferences) -> Unit, onReset: () -> Unit) {
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
        contentPadding = PagePadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenName(title = strings.settingsTitle) }
        item {
            LogCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCell(strings.weight, CalorieCoreFormatter.kilograms(logbook.profile.weightKg))
                    StatCell(strings.height, "${logbook.profile.heightCm} cm")
                    StatCell(strings.bmi, CalorieCoreFormatter.logDecimal(settingsDay.burnEstimate.bmi))
                }
            }
        }
        item {
            RowsCard(SettingsItem.entries) { row ->
                when (row) {
                    SettingsItem.Language -> SettingsRow(
                        icon = LineIcons.Language,
                        label = strings.language,
                        value = strings.languageDisplayName(logbook.settings.language),
                        onClick = { isLanguageDialogOpen = true }
                    )

                    SettingsItem.Theme -> SettingsRow(
                        icon = LineIcons.Theme,
                        label = strings.theme,
                        value = strings.themeDisplayName(logbook.settings.themeMode),
                        onClick = { isThemeDialogOpen = true }
                    )

                    SettingsItem.Legal -> SettingsRow(
                        icon = Icons.Rounded.Info,
                        label = strings.legalAndDataSources,
                        value = null,
                        onClick = { showLegalInfo = true }
                    )
                }
            }
        }
        item { ShelfHeader(strings.data) }
        item {
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = strings.privacyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { isResetDialogOpen = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.resetData)
                    }
                }
            }
        }
    }
}

private enum class SettingsItem { Language, Theme, Legal }

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
