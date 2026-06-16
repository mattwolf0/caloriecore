package app.caloriecore.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.model.logDateText
import app.caloriecore.ui.model.logTimeText
import java.util.Calendar

@Composable
fun LogMomentPicker(
    pickedMillis: Long,
    onValueChange: (Long) -> Unit,
    dateLabel: String,
    timeLabel: String,
    modifier: Modifier = Modifier,
    todayText: String = "Today"
) {
    val context = LocalContext.current

    // Date and time pickers avoid bad typed values.
    fun pickedCalendar(): Calendar = Calendar.getInstance().apply { timeInMillis = pickedMillis }

    fun putDateOnPickedMoment(year: Int, month: Int, day: Int) {
        val calendar = pickedCalendar()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        onValueChange(calendar.timeInMillis)
    }

    fun putTimeOnPickedMoment(hour: Int, minute: Int) {
        val calendar = pickedCalendar()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        onValueChange(calendar.timeInMillis)
    }

    fun nudgePickedDay(delta: Int) {
        val calendar = pickedCalendar()
        calendar.add(Calendar.DAY_OF_YEAR, delta)
        onValueChange(calendar.timeInMillis)
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MomentChip(onClick = { nudgePickedDay(-1) }, text = "-")
            MomentChip(
                modifier = Modifier.weight(1f),
                onClick = {
                    val calendar = pickedCalendar()
                    DatePickerDialog(
                        context,
                        { _, year, month, day -> putDateOnPickedMoment(year, month, day) },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                text = "$dateLabel ${logDateText(pickedMillis)}"
            )
            MomentChip(onClick = { nudgePickedDay(1) }, text = "+")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MomentChip(modifier = Modifier.weight(1f), onClick = { onValueChange(System.currentTimeMillis()) }, text = todayText)
            MomentChip(
                modifier = Modifier.weight(1f),
                onClick = {
                    val calendar = pickedCalendar()
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> putTimeOnPickedMoment(hour, minute) },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                text = "$timeLabel ${logTimeText(pickedMillis)}"
            )
        }
    }
}

@Composable
private fun MomentChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, maxLines = 1)
    }
}
