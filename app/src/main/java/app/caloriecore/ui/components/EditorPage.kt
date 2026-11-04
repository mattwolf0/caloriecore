package app.caloriecore.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing

@Composable
internal fun EditorPage(
    strings: CalorieCoreStrings,
    hasChanges: Boolean,
    onClose: () -> Unit,
    onEditorOpenChange: (Boolean) -> Unit,
    scrollKey: Any? = null,
    content: @Composable (onCancel: () -> Unit) -> Unit
) {
    var askToDiscard by rememberSaveable { mutableStateOf(false) }
    val closeEditor = {
        if (hasChanges) askToDiscard = true else onClose()
    }
    DisposableEffect(Unit) {
        onEditorOpenChange(true)
        onDispose { onEditorOpenChange(false) }
    }
    BackHandler(onBack = closeEditor)
    if (askToDiscard) {
        ConfirmActionDialog(
            title = strings.discardChanges,
            message = strings.discardChangesText,
            confirmText = strings.discard,
            cancelText = strings.keepEditing,
            onConfirm = { askToDiscard = false; onClose() },
            onDismiss = { askToDiscard = false }
        )
    }
    val scrollState = key(scrollKey) { rememberScrollState() }
    Column(
        Modifier.fillMaxSize().verticalScroll(scrollState).padding(UiSpacing.page),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.section)
    ) {
        TextButton(onClick = closeEditor) { Text("‹ ${strings.back}") }
        content(closeEditor)
    }
}
