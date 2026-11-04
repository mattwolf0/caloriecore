package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.ClickableLogCard
import app.caloriecore.ui.components.ConfirmActionDialog
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.NutritionGoal
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing

@Composable
fun SettingsScreen(
    logbook: Logbook,
    strings: CalorieCoreStrings,
    onSettingsChange: (UserPreferences) -> Unit,
    onReset: () -> Unit,
    onSubpageChange: (Boolean) -> Unit = {}
) {
    var isLanguageDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isThemeDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isGoalDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isResetDialogOpen by rememberSaveable { mutableStateOf(false) }
    var showLegalInfo by rememberSaveable { mutableStateOf(false) }

    if (showLegalInfo) {
        DisposableEffect(Unit) {
            onSubpageChange(true)
            onDispose { onSubpageChange(false) }
        }
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

    if (isGoalDialogOpen) {
        GoalPickerDialog(
            selectedGoal = logbook.settings.nutritionGoal,
            strings = strings,
            onGoalSelected = { goal ->
                onSettingsChange(logbook.settings.copy(nutritionGoal = goal))
                isGoalDialogOpen = false
            },
            onDismiss = { isGoalDialogOpen = false }
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
        contentPadding = PaddingValues(UiSpacing.page),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.section)
    ) {
        item { ScreenName(title = strings.settingsTitle) }
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
            ClickableLogCard(onClick = { isGoalDialogOpen = true }) {
                SettingsPickerRow(
                    label = strings.goal,
                    value = strings.goalDisplayName(logbook.settings.nutritionGoal)
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
                Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
                    ShelfHeader(strings.data)
                    Text(
                        text = strings.privacyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { isResetDialogOpen = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text(strings.resetData) }
                }
            }
        }
    }
}

@Composable
private fun SettingsPickerRow(label: String, value: String) {
    ShelfHeader(label, "$value ›")
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
private fun GoalPickerDialog(
    selectedGoal: NutritionGoal,
    strings: CalorieCoreStrings,
    onGoalSelected: (NutritionGoal) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.goal) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                NutritionGoal.entries.forEach { goal ->
                    val isSelected = goal == selectedGoal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onGoalSelected(goal) }
                            )
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = null)
                        Text(
                            text = strings.goalDisplayName(goal),
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
