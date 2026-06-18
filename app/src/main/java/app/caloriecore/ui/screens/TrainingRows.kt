package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.MiniBadge
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.GymGreen

internal fun LazyListScope.trainingRows(
    sessions: List<TrainingSession>,
    plans: List<TrainingPlan>,
    finishedPlanIds: Set<Long>,
    strings: CalorieCoreStrings,
    showPlanBuilder: Boolean,
    onCreatePlan: () -> Unit,
    onStartPlan: (TrainingPlan) -> Unit,
    onEditPlan: (TrainingPlan) -> Unit,
    onDeletePlan: (TrainingPlan) -> Unit,
    onEditSession: (TrainingSession) -> Unit,
    onDeleteSession: (TrainingSession) -> Unit
) {
    item {
        CompletedPlansCard(
            sessions = sessions,
            strings = strings,
            onEdit = onEditSession,
            onDelete = onDeleteSession
        )
    }
    item { ShelfHeader(strings.trainingPlans, "${plans.size} ${strings.items}") }
    if (!showPlanBuilder) {
        item {
            SolidActionButton(
                text = strings.createPlan,
                onClick = onCreatePlan
            )
        }
    }
    if (plans.isEmpty()) {
        item { EmptyTrainingCard(strings.noPlans) }
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
private fun CompletedPlansCard(
    sessions: List<TrainingSession>,
    strings: CalorieCoreStrings,
    onEdit: (TrainingSession) -> Unit,
    onDelete: (TrainingSession) -> Unit
) {
    LogCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShelfHeader(strings.completedToday, "${sessions.size} ${strings.items}")
            if (sessions.isEmpty()) {
                Text(
                    text = strings.noEntries,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sessions.forEach { session ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(session.title, fontWeight = FontWeight.Bold)
                            Text(
                                "${session.totalSets} ${strings.sets.lowercase()} | " +
                                    CalorieCoreFormatter.kilograms(session.totalVolumeKg),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            TextButton(onClick = { onEdit(session) }) { Text(strings.edit) }
                            TextButton(onClick = { onDelete(session) }) { Text(strings.delete) }
                        }
                    }
                }
            }
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
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = plan.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isCompletedToday) MiniBadge(strings.doneToday, color = GymGreen)
                }
            }
            plan.exercises.take(4).forEach { exercise ->
                Text(
                    text = "${exercise.name}: ${exercise.plannedSets.joinToString(" | ") { formatPlannedBlock(it) }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (plan.exercises.size > 4) {
                Text(
                    text = "+${plan.exercises.size - 4} ${strings.exercises.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onStart) { Text(strings.startPlan) }
                TextButton(onClick = onEdit) { Text(strings.editPlan) }
                TextButton(onClick = onDelete) { Text(strings.delete) }
            }
        }
    }
}

@Composable
private fun EmptyTrainingCard(message: String) {
    LogCard {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
