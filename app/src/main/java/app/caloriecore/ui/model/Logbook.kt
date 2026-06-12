package app.caloriecore.ui.model

import kotlin.math.abs

data class Logbook(
    val selectedDateTime: Long = phoneNowMillis(),
    val profile: BodySnapshot = BodySnapshot(loggedAt = selectedDateTime),
    val bodyHistory: List<BodySnapshot> = listOf(profile),
    val foodEntries: List<FoodEntry> = emptyList(),
    val trainingSessions: List<TrainingSession> = emptyList(),
    val trainingPlans: List<TrainingPlan> = emptyList(),
    val settings: UserPreferences = UserPreferences()
)

fun <T> List<T>.putById(id: Long, replacement: T, idSelector: (T) -> Long): List<T> =
    if (any { idSelector(it) == id }) {
        map { if (idSelector(it) == id) replacement else it }
    } else {
        listOf(replacement) + this
    }

fun nearestBodyCheckIn(profiles: List<BodySnapshot>, selectedDateTime: Long): BodySnapshot {
    // Use the nearest saved body data for the selected day.
    val exact = profiles.minByOrNull { abs(it.loggedAt - selectedDateTime) }
        ?.takeIf { abs(it.loggedAt - selectedDateTime) <= 59_999L }
    if (exact != null) return exact

    val previous = profiles
        .filter { it.loggedAt <= phoneDayEndMillis(selectedDateTime) }
        .maxByOrNull { it.loggedAt }
    val fallback = previous ?: profiles.maxByOrNull { it.loggedAt } ?: BodySnapshot(loggedAt = selectedDateTime)
    return fallback.copy(id = newLogId(), loggedAt = selectedDateTime)
}

fun Logbook.withPickedMoment(nextDateTime: Long): Logbook = copy(
    selectedDateTime = nextDateTime,
    profile = nearestBodyCheckIn(bodyHistory, nextDateTime)
)

fun Logbook.withBodyCheckIn(nextProfile: BodySnapshot): Logbook {
    val normalized = nextProfile.copy(loggedAt = selectedDateTime)
    val nextHistory = bodyHistory
        .putById(normalized.id, normalized) { it.id }
        .sortedByDescending { it.loggedAt }
    return copy(profile = normalized, bodyHistory = nextHistory)
}

fun Logbook.foodRowsForPickedDay(): List<FoodEntry> = mealsOnPhoneDay(foodEntries, selectedDateTime)

fun Logbook.gymRowsForPickedDay(): List<TrainingSession> =
    gymLogsOnPhoneDay(trainingSessions, selectedDateTime)
