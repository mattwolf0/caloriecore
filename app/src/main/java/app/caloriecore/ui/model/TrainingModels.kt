package app.caloriecore.ui.model

data class LoggedSet(
    val id: Long = newLogId(),
    val loggedAt: Long = phoneNowMillis(),
    val reps: Int,
    val weightKg: Double,
    val plannedReps: Int? = null,
    val plannedWeightKg: Double? = null
) {
    val volumeKg: Double
        get() = reps.coerceAtLeast(0) * weightKg.coerceAtLeast(0.0)
}

data class LoggedExercise(
    val id: Long = newLogId(),
    val sessionId: Long = 0L,
    val name: String,
    val target: String = "",
    val position: Int = 0,
    val sets: List<LoggedSet> = emptyList()
) {
    val totalVolumeKg: Double
        get() = sets.sumOf { it.volumeKg }

    val bestSetKg: Double
        get() = sets.maxOfOrNull { it.weightKg } ?: 0.0
}

data class TrainingSession(
    val id: Long = newLogId(),
    val programId: Long? = null,
    val title: String,
    val loggedAt: Long = phoneNowMillis(),
    val exercises: List<LoggedExercise> = emptyList()
) {
    val totalVolumeKg: Double
        get() = exercises.sumOf { it.totalVolumeKg }

    val totalSets: Int
        get() = exercises.sumOf { it.sets.size }
}

data class PlannedSetBlock(
    val id: Long = newLogId(),
    val setCount: Int = 3,
    val reps: Int = 10,
    val weightKg: Double = 0.0
) {
    val label: String
        get() = "${setCount.coerceAtLeast(1)} x ${reps.coerceAtLeast(1)} @ ${weightKg.coerceAtLeast(0.0)} kg"

    fun expand(loggedAt: Long): List<LoggedSet> = List(setCount.coerceAtLeast(1)) {
        val plannedReps = reps.coerceAtLeast(1)
        val plannedKg = weightKg.coerceAtLeast(0.0)
        LoggedSet(
            loggedAt = loggedAt,
            reps = plannedReps,
            weightKg = plannedKg,
            plannedReps = plannedReps,
            plannedWeightKg = plannedKg
        )
    }
}

data class TrainingPlanExercise(
    val id: Long = newLogId(),
    val name: String,
    val target: String = "",
    val position: Int = 0,
    val plannedSets: List<PlannedSetBlock> = emptyList()
)

data class TrainingPlan(
    val id: Long = newLogId(),
    val title: String,
    val focus: String = "",
    val daysPerWeek: Int = 3,
    val exercises: List<TrainingPlanExercise> = emptyList()
)

fun TrainingPlan.startSheetFor(loggedAt: Long): TrainingSession = TrainingSession(
    programId = id,
    title = title,
    loggedAt = loggedAt,
    exercises = exercises.mapIndexed { index, exercise ->
        LoggedExercise(
            name = exercise.name,
            target = exercise.target,
            position = index,
            sets = exercise.plannedSets.flatMap { it.expand(loggedAt) }
        )
    }
)

fun gymLogsOnPhoneDay(sessions: List<TrainingSession>, selectedDateTime: Long): List<TrainingSession> = sessions
    .filter { samePhoneDay(it.loggedAt, selectedDateTime) }
    .sortedByDescending { it.loggedAt }

fun finishedPlanIdsOnPhoneDay(sessions: List<TrainingSession>, selectedDateTime: Long): Set<Long> =
    gymLogsOnPhoneDay(sessions, selectedDateTime)
        .mapNotNull { it.programId }
        .toSet()
