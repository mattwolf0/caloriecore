package app.caloriecore.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import app.caloriecore.ui.components.EditorActions
import app.caloriecore.ui.components.InputRow
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MiniBadge
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.ActivityCatalogItem
import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.activeCaloriesTotal
import app.caloriecore.ui.model.searchActivities
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.FoodAmber
import app.caloriecore.ui.theme.UiSpacing

@Composable
internal fun ActivityLogCard(
    entries: List<ActivityEntry>,
    strings: CalorieCoreStrings,
    isEditorOpen: Boolean,
    onAdd: () -> Unit,
    onEdit: (ActivityEntry) -> Unit,
    onDelete: (ActivityEntry) -> Unit
) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
            ShelfHeader(
                title = strings.activity,
                trailing = CalorieCoreFormatter.kcal(entries.activeCaloriesTotal())
            )
            if (!isEditorOpen) {
                SolidActionButton(text = strings.addActivity, onClick = onAdd)
            }
            if (entries.isNotEmpty()) {
                entries.forEach { entry ->
                    ActivityRow(
                        entry = entry,
                        strings = strings,
                        onEdit = { onEdit(entry) },
                        onDelete = { onDelete(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(
    entry: ActivityEntry,
    strings: CalorieCoreStrings,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.tight)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.tight)) {
            Text(entry.name, style = MaterialTheme.typography.titleMedium)
            val minutes = entry.durationMinutes?.let { "$it ${strings.minutes.lowercase()} · " }.orEmpty()
            Text(
                text = "$minutes${CalorieCoreFormatter.kcal(entry.activeCalories)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        InputRow {
            TextButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text(strings.edit) }
            TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) { Text(strings.delete) }
        }
    }
}

@Composable
internal fun ActivityEditor(
    state: ActivityDraftState,
    catalog: List<ActivityCatalogItem>,
    language: UiLanguage,
    strings: CalorieCoreStrings,
    onSave: (ActivityEntry) -> Unit,
    onClose: () -> Unit
) {
    val draft = state.draft
    val results = if (draft.mode == ActivityMode.Catalog && draft.catalogCode == null) {
        searchActivities(catalog, draft.searchText, language)
    } else {
        emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
        ScreenName(if (draft.editingId == null) strings.addActivity else strings.updateActivity)
        LogCard {
            Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.content)) {
                InputRow {
                    FilterChip(
                        selected = draft.mode == ActivityMode.Catalog,
                        onClick = state::useCatalog,
                        label = { Text(strings.activityList) },
                        leadingIcon = if (draft.mode == ActivityMode.Catalog) ({ Text("✓") }) else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = draft.mode == ActivityMode.Other,
                        onClick = state::useOther,
                        label = { Text(strings.otherActivity) },
                        leadingIcon = if (draft.mode == ActivityMode.Other) ({ Text("✓") }) else null,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(strings.activityDoubleCountWarning, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        LogCard {
            Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.section)) {
                if (draft.mode == ActivityMode.Catalog) {
                    CatalogActivityFields(
                        state = state,
                        results = results,
                        language = language,
                        strings = strings
                    )
                } else {
                    OtherActivityFields(state = state, strings = strings)
                }
            }
        }
        EditorActions(
            saveText = if (draft.editingId == null) strings.addActivity else strings.updateActivity,
            canSave = draft.canSave,
            strings = strings,
            onSave = { draft.buildEntry()?.let(onSave) },
            onCancel = onClose
        )
    }
}

@Composable
private fun CatalogActivityFields(
    state: ActivityDraftState,
    results: List<ActivityCatalogItem>,
    language: UiLanguage,
    strings: CalorieCoreStrings
) {
    val draft = state.draft
    val focus = LocalFocusManager.current
    if (draft.catalogCode == null) OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = draft.searchText,
        onValueChange = state::updateSearch,
        label = { Text(strings.searchActivity) },
        shape = MaterialTheme.shapes.small, singleLine = true
    )
    if (results.isNotEmpty()) Column {
        results.forEach { item ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clickable { focus.clearFocus(); state.pick(item, language) }
                    .padding(vertical = UiSpacing.content),
                verticalArrangement = Arrangement.spacedBy(UiSpacing.tight)
            ) {
                Text(item.nameFor(language), style = MaterialTheme.typography.bodyMedium)
                Text("${CalorieCoreFormatter.logDecimal(item.met)} MET",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    if (draft.catalogCode == null && results.isEmpty()) {
        Text(strings.noActivityResults, style = MaterialTheme.typography.bodySmall)
    }
    if (draft.catalogCode != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UiSpacing.content),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(draft.pickedName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            MiniBadge("${CalorieCoreFormatter.logDecimal(draft.met ?: 0.0)} MET", color = FoodAmber)
        }
        TextButton(onClick = { state.updateSearch("") }) { Text(strings.changeActivity) }
        NumberInput(
            label = strings.minutes,
            numberText = draft.minutes,
            onValueChange = state::updateMinutes,
            isError = draft.minutes.isNotEmpty() && draft.minutes.toIntOrNull() !in 1..1440
        )
        if (draft.minutes.toIntOrNull() !in 1..1440) {
            Text(strings.activityMinutesHint, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = "${strings.weightUsed}: ${CalorieCoreFormatter.kilograms(draft.weightKg)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${strings.estimatedBurn}: ${CalorieCoreFormatter.kcal(draft.calculatedCalories)}",
            fontWeight = FontWeight.Bold
        )
    }
    Text(
        text = strings.activityCatalogSource,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun OtherActivityFields(state: ActivityDraftState, strings: CalorieCoreStrings) {
    val draft = state.draft
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = draft.otherName,
        onValueChange = state::updateOtherName,
        label = { Text(strings.otherActivityName) },
        shape = MaterialTheme.shapes.small, singleLine = true
    )
    NumberInput(
        label = strings.activeCalories,
        numberText = draft.otherCalories,
        onValueChange = state::updateOtherCalories,
        suffix = "kcal"
    )
}
