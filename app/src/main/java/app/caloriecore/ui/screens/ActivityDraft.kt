package app.caloriecore.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.caloriecore.ui.components.keepLogNumberText
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.ActivityCatalogItem
import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.ActivitySource
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.activeCaloriesFor
import app.caloriecore.ui.model.newLogId

internal enum class ActivityMode {
    Catalog,
    Other
}

internal data class ActivityDraft(
    val isOpen: Boolean = false,
    val editingId: Long? = null,
    val loggedAt: Long,
    val mode: ActivityMode = ActivityMode.Catalog,
    val searchText: String = "",
    val catalogCode: String? = null,
    val pickedName: String = "",
    val met: Double? = null,
    val minutes: String = "30",
    val weightKg: Double,
    val otherName: String = "",
    val otherCalories: String = ""
) {
    val calculatedCalories: Int
        get() = activeCaloriesFor(
            met = met ?: 0.0,
            weightKg = weightKg,
            minutes = minutes.toIntOrNull() ?: 0
        )

    val canSave: Boolean
        get() = when (mode) {
            ActivityMode.Catalog -> catalogCode != null &&
                pickedName.isNotBlank() &&
                minutes.toIntOrNull() in 1..1440 &&
                met?.let { it.isFinite() && it > 0.0 } == true &&
                weightKg.isFinite() && weightKg > 0.0 &&
                calculatedCalories > 0

            ActivityMode.Other -> otherName.isNotBlank() &&
                (otherCalories.toIntOrNull() ?: 0) > 0
        }

    fun buildEntry(): ActivityEntry? {
        if (!canSave) return null
        return when (mode) {
            ActivityMode.Catalog -> ActivityEntry(
                id = editingId ?: newLogId(),
                loggedAt = loggedAt,
                catalogCode = catalogCode,
                name = pickedName,
                source = ActivitySource.Compendium,
                durationMinutes = minutes.toIntOrNull(),
                met = met,
                weightKg = weightKg,
                activeCalories = calculatedCalories
            )

            ActivityMode.Other -> ActivityEntry(
                id = editingId ?: newLogId(),
                loggedAt = loggedAt,
                name = otherName.trim(),
                source = ActivitySource.Manual,
                activeCalories = otherCalories.toIntOrNull() ?: 0
            )
        }
    }
}

internal class ActivityDraftState(private val draftState: MutableState<ActivityDraft>) {
    var draft by draftState
        private set

    fun start(selectedDateTime: Long, weightKg: Double) {
        draft = ActivityDraft(isOpen = true, loggedAt = selectedDateTime, weightKg = weightKg)
    }

    fun close(selectedDateTime: Long, weightKg: Double) {
        draft = ActivityDraft(loggedAt = selectedDateTime, weightKg = weightKg)
    }

    fun followSelectedMoment(selectedDateTime: Long, weightKg: Double) {
        if (draft.editingId == null) {
            draft = draft.copy(loggedAt = selectedDateTime, weightKg = weightKg)
        }
    }

    fun reopen(entry: ActivityEntry, fallbackWeightKg: Double) {
        draft = ActivityDraft(
            isOpen = true,
            editingId = entry.id,
            loggedAt = entry.loggedAt,
            mode = if (entry.source == ActivitySource.Compendium) ActivityMode.Catalog else ActivityMode.Other,
            searchText = entry.name,
            catalogCode = entry.catalogCode,
            pickedName = entry.name,
            met = entry.met,
            minutes = entry.durationMinutes?.toString() ?: "30",
            weightKg = entry.weightKg ?: fallbackWeightKg,
            otherName = if (entry.source == ActivitySource.Manual) entry.name else "",
            otherCalories = if (entry.source == ActivitySource.Manual) entry.activeCalories.toString() else ""
        )
    }

    fun useCatalog() {
        draft = draft.copy(mode = ActivityMode.Catalog)
    }

    fun useOther() {
        draft = draft.copy(mode = ActivityMode.Other)
    }

    fun updateSearch(text: String) {
        draft = draft.copy(
            searchText = text,
            catalogCode = null,
            pickedName = "",
            met = null
        )
    }

    fun pick(item: ActivityCatalogItem, language: UiLanguage) {
        val pickedName = item.nameFor(language)
        draft = draft.copy(
            searchText = pickedName,
            catalogCode = item.code,
            pickedName = pickedName,
            met = item.met
        )
    }

    fun updateMinutes(text: String) {
        draft = draft.copy(minutes = keepLogNumberText(text))
    }

    fun updateOtherName(text: String) {
        draft = draft.copy(otherName = text)
    }

    fun updateOtherCalories(text: String) {
        draft = draft.copy(otherCalories = keepLogNumberText(text))
    }
}

private val ActivityDraftSaver = listSaver<MutableState<ActivityDraft>, Any>(
    save = { state ->
        val draft = state.value
        listOf(
            draft.isOpen,
            draft.editingId != null,
            draft.editingId ?: 0L,
            draft.loggedAt,
            draft.mode.name,
            draft.searchText,
            draft.catalogCode.orEmpty(),
            draft.pickedName,
            draft.met?.toString().orEmpty(),
            draft.minutes,
            CalorieCoreFormatter.logDecimal(draft.weightKg, maxDecimals = 2),
            draft.otherName,
            draft.otherCalories
        )
    },
    restore = { values ->
        mutableStateOf(
            ActivityDraft(
                isOpen = values[0] as Boolean,
                editingId = if (values[1] as Boolean) values[2] as Long else null,
                loggedAt = values[3] as Long,
                mode = ActivityMode.valueOf(values[4] as String),
                searchText = values[5] as String,
                catalogCode = (values[6] as String).takeIf { it.isNotEmpty() },
                pickedName = values[7] as String,
                met = (values[8] as String).toDoubleOrNull(),
                minutes = values[9] as String,
                weightKg = (values[10] as String).toDoubleOrNull() ?: 0.0,
                otherName = values[11] as String,
                otherCalories = values[12] as String
            )
        )
    }
)

@Composable
internal fun rememberActivityDraft(selectedDateTime: Long, weightKg: Double): ActivityDraftState {
    val draftState = rememberSaveable(saver = ActivityDraftSaver) {
        mutableStateOf(ActivityDraft(loggedAt = selectedDateTime, weightKg = weightKg))
    }
    return remember { ActivityDraftState(draftState) }
}
