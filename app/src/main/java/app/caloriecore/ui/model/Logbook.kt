package app.caloriecore.ui.model

data class Logbook(
    val selectedDateTime: Long = phoneNowMillis(),
    val profile: BodySnapshot = BodySnapshot(loggedAt = selectedDateTime),
    val bodyHistory: List<BodySnapshot> = listOf(profile),
    val foodEntries: List<FoodEntry> = emptyList(),
    val activityEntries: List<ActivityEntry> = emptyList(),
    val trainingSessions: List<TrainingSession> = emptyList(),
    val trainingPlans: List<TrainingPlan> = emptyList(),
    val settings: UserPreferences = UserPreferences()
)

fun <T> List<T>.putById(id: Long, replacement: T, idSelector: (T) -> Long): List<T> = if (any { idSelector(it) == id }) {
    map { if (idSelector(it) == id) replacement else it }
} else {
    listOf(replacement) + this
}

fun nearestBodyCheckIn(profiles: List<BodySnapshot>, selectedDateTime: Long): BodySnapshot {
    val dayProfile = profiles.filter { samePhoneDay(it.loggedAt, selectedDateTime) }
        .maxByOrNull { it.loggedAt }
    if (dayProfile != null) return dayProfile

    val previous = profiles
        .filter { it.loggedAt <= phoneDayEndMillis(selectedDateTime) }
        .maxByOrNull { it.loggedAt }
    val fallback = previous ?: profiles.minByOrNull { it.loggedAt }
        ?: return BodySnapshot(loggedAt = selectedDateTime, hasWeightEntry = false)
    return fallback.copy(
        id = newLogId(),
        loggedAt = selectedDateTime,
        sleepHours = 0.0,
        watchActiveCalories = 0,
        steps = 0,
        plannedWorkoutCalories = 0,
        hasWeightEntry = false
    )
}

fun Logbook.withPickedMoment(nextDateTime: Long): Logbook = copy(
    selectedDateTime = nextDateTime,
    profile = nearestBodyCheckIn(bodyHistory, nextDateTime)
)

fun Logbook.withBodyCheckIn(nextProfile: BodySnapshot): Logbook {
    val existing = bodyHistory.find { it.id == nextProfile.id }
    val sameDay = existing != null && samePhoneDay(existing.loggedAt, selectedDateTime)
    val normalized = nextProfile.copy(
        id = if (existing != null && !sameDay) newLogId() else nextProfile.id,
        loggedAt = if (sameDay) checkNotNull(existing).loggedAt else selectedDateTime
    )
    val nextHistory = bodyHistory
        .putById(normalized.id, normalized) { it.id }
        .sortedByDescending { it.loggedAt }
    return copy(profile = normalized, bodyHistory = nextHistory)
}

fun Logbook.foodRowsForPickedDay(): List<FoodEntry> = mealsOnPhoneDay(foodEntries, selectedDateTime)

fun Logbook.activityRowsForPickedDay(): List<ActivityEntry> = activitiesOnPhoneDay(activityEntries, selectedDateTime)

fun Logbook.gymRowsForPickedDay(): List<TrainingSession> = gymLogsOnPhoneDay(trainingSessions, selectedDateTime)
