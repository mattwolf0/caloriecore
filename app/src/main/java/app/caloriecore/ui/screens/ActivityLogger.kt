package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MiniBadge
import app.caloriecore.ui.components.NumberInput
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

@Composable
internal fun ActivityLogCard(
    entries: List<ActivityEntry>,
    strings: CalorieCoreStrings,
    editorOpen: Boolean,
    onAdd: () -> Unit,
    onEdit: (ActivityEntry) -> Unit,
    onDelete: (ActivityEntry) -> Unit
) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            ShelfHeader(
                title = strings.activity,
                trailing = CalorieCoreFormatter.kcal(entries.activeCaloriesTotal())
            )
            Text(
                text = strings.activityDoubleCountWarning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!editorOpen) {
                SolidActionButton(text = strings.addActivity, onClick = onAdd)
            }
            if (entries.isEmpty()) {
                Text(
                    text = strings.noActivities,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.name, fontWeight = FontWeight.Bold)
            val minutes = entry.durationMinutes?.let { "$it ${strings.minutes.lowercase()} · " }.orEmpty()
            Text(
                text = "$minutes${CalorieCoreFormatter.kcal(entry.activeCalories)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row {
            TextButton(onClick = onEdit) { Text(strings.edit) }
            TextButton(onClick = onDelete) { Text(strings.delete) }
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

    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            ShelfHeader(
                title = if (draft.editingId == null) strings.addActivity else strings.updateActivity
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    enabled = draft.mode != ActivityMode.Catalog,
                    onClick = state::useCatalog
                ) { Text(strings.activityList) }
                TextButton(
                    enabled = draft.mode != ActivityMode.Other,
                    onClick = state::useOther
                ) { Text(strings.otherActivity) }
            }

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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SolidActionButton(
                    text = if (draft.editingId == null) strings.addActivity else strings.updateActivity,
                    enabled = draft.canSave,
                    modifier = Modifier.weight(1f),
                    onClick = { draft.buildEntry()?.let(onSave) }
                )
                TextButton(onClick = onClose) { Text(strings.close) }
            }
        }
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
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = draft.searchText,
        onValueChange = state::updateSearch,
        label = { Text(strings.searchActivity) },
        singleLine = true
    )
    results.forEach { item ->
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { state.pick(item, language) }
        ) {
            Text(
                text = "${item.nameFor(language)} · ${CalorieCoreFormatter.logDecimal(item.met)} MET",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    if (draft.catalogCode != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(draft.pickedName, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            MiniBadge("${CalorieCoreFormatter.logDecimal(draft.met ?: 0.0)} MET", color = FoodAmber)
        }
        NumberInput(
            label = strings.minutes,
            numberText = draft.minutes,
            onValueChange = state::updateMinutes
        )
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
        singleLine = true
    )
    NumberInput(
        label = strings.activeCalories,
        numberText = draft.otherCalories,
        onValueChange = state::updateOtherCalories,
        suffix = "kcal"
    )
}
