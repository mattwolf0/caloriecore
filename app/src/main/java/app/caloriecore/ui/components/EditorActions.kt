package app.caloriecore.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.caloriecore.ui.text.CalorieCoreStrings

@Composable
internal fun EditorActions(
    saveText: String,
    canSave: Boolean,
    strings: CalorieCoreStrings,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    InputRow {
        SecondaryActionButton(strings.cancel, onCancel, Modifier.weight(1f))
        SolidActionButton(saveText, onSave, Modifier.weight(1f), enabled = canSave)
    }
}
