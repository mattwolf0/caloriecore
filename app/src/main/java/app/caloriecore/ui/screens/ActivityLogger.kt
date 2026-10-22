package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.EmptyCard
import app.caloriecore.ui.components.LogRow
import app.caloriecore.ui.components.MiniBadge
import app.caloriecore.ui.components.NumberInput
import app.caloriecore.ui.components.PickRow
import app.caloriecore.ui.components.RowsCard
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.ActivityCatalogItem
import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.searchActivities
import app.caloriecore.ui.text.CalorieCoreStrings

internal fun LazyListScope.activityRows(
    entries: List<ActivityEntry>,
    strings: CalorieCoreStrings,
    onAdd: () -> Unit,
    onEdit: (ActivityEntry) -> Unit,
    onDelete: (ActivityEntry) -> Unit
) {
    item { ShelfHeader(strings.activity, actionLabel = strings.addActivity, onAction = onAdd) }
    if (entries.isEmpty()) {
        item { EmptyCard(strings.noActivities) }
    } else {
        item {
            RowsCard(entries) { entry ->
                LogRow(
                    title = entry.name,
                    subtitle = entry.durationMinutes?.let { "$it ${strings.minutes.lowercase()}" },
                    trailingText = CalorieCoreFormatter.kcal(entry.activeCalories),
                    onClick = { onEdit(entry) },
                    onDelete = { onDelete(entry) },
                    deleteLabel = strings.delete
                )
            }
        }
    }
    item {
        Text(
            text = strings.activityDoubleCountWarning,
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun ActivityEditor(
    state: ActivityDraftState,
    catalog: List<ActivityCatalogItem>,
    language: UiLanguage,
    strings: CalorieCoreStrings,
    onSave: (ActivityEntry) -> Unit
) {
    val draft = state.draft
    val results = if (draft.mode == ActivityMode.Catalog && draft.catalogCode == null) {
        searchActivities(catalog, draft.searchText, language)
    } else {
        emptyList()
    }

    PickRow(
        options = ActivityMode.entries,
        selected = draft.mode,
        label = { mode -> if (mode == ActivityMode.Catalog) strings.activityList else strings.otherActivity },
        onSelect = { mode -> if (mode == ActivityMode.Catalog) state.useCatalog() else state.useOther() }
    )

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

    SolidActionButton(
        text = if (draft.editingId == null) strings.addActivity else strings.updateActivity,
        enabled = draft.canSave,
        onClick = { draft.buildEntry()?.let(onSave) }
    )
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
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        singleLine = true
    )
    if (results.isNotEmpty()) {
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Column {
                results.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    Surface(onClick = { state.pick(item, language) }, color = Color.Transparent) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.nameFor(language),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${CalorieCoreFormatter.logDecimal(item.met)} MET",
                                modifier = Modifier.padding(start = 12.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    if (draft.catalogCode != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(draft.pickedName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            MiniBadge("${CalorieCoreFormatter.logDecimal(draft.met ?: 0.0)} MET", color = MaterialTheme.colorScheme.secondary)
        }
        NumberInput(
            label = strings.minutes,
            numberText = draft.minutes,
            onValueChange = state::updateMinutes
        )
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = strings.estimatedBurn,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = CalorieCoreFormatter.kcal(draft.calculatedCalories),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${strings.weightUsed}: ${CalorieCoreFormatter.kilograms(draft.weightKg)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
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
