package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.EmptyCard
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.LogRow
import app.caloriecore.ui.components.MiniBadge
import app.caloriecore.ui.components.RowsCard
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.text.CalorieCoreStrings

internal fun LazyListScope.trainingRows(
    sessions: List<TrainingSession>,
    plans: List<TrainingPlan>,
    finishedPlanIds: Set<Long>,
    strings: CalorieCoreStrings,
    onCreatePlan: () -> Unit,
    onStartPlan: (TrainingPlan) -> Unit,
    onEditPlan: (TrainingPlan) -> Unit,
    onDeletePlan: (TrainingPlan) -> Unit,
    onEditSession: (TrainingSession) -> Unit,
    onDeleteSession: (TrainingSession) -> Unit
) {
    item { ShelfHeader(strings.completedToday, "${sessions.size} ${strings.items}") }
    if (sessions.isEmpty()) {
        item { EmptyCard(strings.noEntries) }
    } else {
        item {
            RowsCard(sessions) { session ->
                LogRow(
                    title = session.title,
                    subtitle = "${session.totalSets} ${strings.sets.lowercase()} · " +
                        CalorieCoreFormatter.kilograms(session.totalVolumeKg),
                    onClick = { onEditSession(session) },
                    onDelete = { onDeleteSession(session) },
                    deleteLabel = strings.delete
                )
            }
        }
    }
    item { ShelfHeader(strings.trainingPlans, actionLabel = strings.createPlan, onAction = onCreatePlan) }
    if (plans.isEmpty()) {
        item { EmptyCard(strings.noPlans) }
    } else {
        items(plans, key = { it.id }) { plan ->
            TrainingPlanRow(
                plan = plan,
                strings = strings,
                isCompletedToday = plan.id in finishedPlanIds,
                onStart = { onStartPlan(plan) },
                onEdit = { onEditPlan(plan) },
                onDelete = { onDeletePlan(plan) }
            )
        }
    }
}

@Composable
private fun TrainingPlanRow(
    plan: TrainingPlan,
    strings: CalorieCoreStrings,
    isCompletedToday: Boolean,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = plan.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isCompletedToday) MiniBadge(strings.doneToday)
            }
            plan.exercises.take(4).forEach { exercise ->
                Text(
                    text = "${exercise.name} · ${exercise.plannedSets.joinToString(", ") { formatPlannedBlock(it) }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (plan.exercises.size > 4) {
                Text(
                    text = "+${plan.exercises.size - 4} ${strings.exercises.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onStart) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.startPlan)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = strings.editPlan,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = strings.delete,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
