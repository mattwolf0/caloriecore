package app.caloriecore.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.model.logDateText
import app.caloriecore.ui.model.logTimeText
import app.caloriecore.ui.model.samePhoneDay
import app.caloriecore.ui.theme.LineIcons
import java.util.Calendar
import java.util.TimeZone

@Composable
fun LogMomentPicker(
    pickedMillis: Long,
    onValueChange: (Long) -> Unit,
    dateLabel: String,
    timeLabel: String,
    modifier: Modifier = Modifier,
    todayText: String = "Today",
    previousDayText: String = "Previous day",
    nextDayText: String = "Next day"
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val isToday = samePhoneDay(pickedMillis, System.currentTimeMillis())
    val pickedDateText = if (isToday) todayText else logDateText(pickedMillis)

    if (showDatePicker) {
        DateDialog(
            pickedMillis = pickedMillis,
            onPicked = {
                onValueChange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    if (showTimePicker) {
        TimeDialog(
            pickedMillis = pickedMillis,
            title = timeLabel,
            onPicked = {
                onValueChange(it)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateChip(
            text = pickedDateText,
            icon = Icons.Rounded.DateRange,
            contentDescription = "$dateLabel $pickedDateText",
            onClick = { showDatePicker = true },
            modifier = Modifier.weight(1.4f)
        )
        DateChip(
            text = logTimeText(pickedMillis),
            icon = LineIcons.Today,
            contentDescription = "$timeLabel ${logTimeText(pickedMillis)}",
            onClick = { showTimePicker = true },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DayPicker(
    pickedMillis: Long,
    onValueChange: (Long) -> Unit,
    dateLabel: String,
    todayText: String,
    previousDayText: String,
    nextDayText: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val isToday = samePhoneDay(pickedMillis, System.currentTimeMillis())
    val pickedDateText = if (isToday) {
        "$todayText · ${logDateText(pickedMillis)}"
    } else {
        logDateText(pickedMillis)
    }

    fun nudgePickedDay(delta: Int) {
        val calendar = Calendar.getInstance().apply { timeInMillis = pickedMillis }
        calendar.add(Calendar.DAY_OF_YEAR, delta)
        onValueChange(calendar.timeInMillis)
    }

    if (showDatePicker) {
        DateDialog(
            pickedMillis = pickedMillis,
            onPicked = {
                onValueChange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(onClick = { nudgePickedDay(-1) }) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = previousDayText)
        }
        DateChip(
            text = pickedDateText,
            icon = Icons.Rounded.DateRange,
            contentDescription = "$dateLabel $pickedDateText",
            onClick = { showDatePicker = true },
            modifier = Modifier.weight(1f)
        )
        FilledTonalIconButton(onClick = { nudgePickedDay(1) }) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = nextDayText)
        }
        if (!isToday) {
            TextButton(onClick = { onValueChange(System.currentTimeMillis()) }) { Text(todayText) }
        }
    }
}

@Composable
private fun DateChip(
    text: String,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 44.dp)
            .semantics { this.contentDescription = contentDescription },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDialog(pickedMillis: Long, onPicked: (Long) -> Unit, onDismiss: () -> Unit) {
    val local = Calendar.getInstance().apply { timeInMillis = pickedMillis }
    val utcMidnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = utcMidnight)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val picked = pickerState.selectedDateMillis
                    if (picked == null) {
                        onDismiss()
                    } else {
                        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = picked }
                        val result = Calendar.getInstance().apply {
                            timeInMillis = pickedMillis
                            set(Calendar.YEAR, utc.get(Calendar.YEAR))
                            set(Calendar.MONTH, utc.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH))
                        }
                        onPicked(result.timeInMillis)
                    }
                }
            ) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    ) {
        DatePicker(state = pickerState, showModeToggle = false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(pickedMillis: Long, title: String, onPicked: (Long) -> Unit, onDismiss: () -> Unit) {
    val local = Calendar.getInstance().apply { timeInMillis = pickedMillis }
    val pickerState = rememberTimePickerState(
        initialHour = local.get(Calendar.HOUR_OF_DAY),
        initialMinute = local.get(Calendar.MINUTE),
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(
                onClick = {
                    val result = Calendar.getInstance().apply {
                        timeInMillis = pickedMillis
                        set(Calendar.HOUR_OF_DAY, pickerState.hour)
                        set(Calendar.MINUTE, pickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onPicked(result.timeInMillis)
                }
            ) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}
