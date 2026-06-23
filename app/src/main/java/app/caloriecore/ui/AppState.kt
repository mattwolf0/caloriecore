package app.caloriecore.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.caloriecore.data.LogbookRepository
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.model.putById
import app.caloriecore.ui.model.withBodyCheckIn
import app.caloriecore.ui.model.withPickedMoment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AppState internal constructor(
    private val repository: LogbookRepository,
    private val saveScope: CoroutineScope
) {
    private val saveMutex = Mutex()

    var logbook by mutableStateOf<Logbook?>(null)
        private set

    suspend fun load() {
        logbook = withContext(Dispatchers.IO) { repository.load() }
    }

    fun jumpToMoment(nextDateTime: Long) {
        logbook = logbook?.withPickedMoment(nextDateTime)
    }

    fun saveBodyEntry(profile: BodySnapshot) {
        val current = logbook ?: return
        saveChanges(current.withBodyCheckIn(profile))
    }

    fun saveMeal(meal: FoodEntry) {
        val current = logbook ?: return
        saveChanges(
            current.copy(
                foodEntries = current.foodEntries
                    .putById(meal.id, meal) { it.id }
                    .sortedByDescending { it.loggedAt }
            )
        )
    }

    fun deleteMeal(id: Long) {
        val current = logbook ?: return
        saveChanges(
            current.copy(
                foodEntries = current.foodEntries.filterNot { it.id == id }
            )
        )
    }

    fun savePlan(plan: TrainingPlan) {
        val current = logbook ?: return
        saveChanges(
            current.copy(
                trainingPlans = current.trainingPlans
                    .putById(plan.id, plan) { it.id }
                    .sortedBy { it.title.lowercase() }
            )
        )
    }

    fun deletePlan(id: Long) {
        val current = logbook ?: return
        saveChanges(
            current.copy(
                trainingPlans = current.trainingPlans.filterNot { it.id == id },
                trainingSessions = current.trainingSessions.map { session ->
                    if (session.programId == id) session.copy(programId = null) else session
                }
            )
        )
    }

    fun saveWorkout(session: TrainingSession) {
        val current = logbook ?: return
        saveChanges(
            current.copy(
                trainingSessions = current.trainingSessions
                    .putById(session.id, session) { it.id }
                    .sortedByDescending { it.loggedAt }
            )
        )
    }

    fun deleteWorkout(id: Long) {
        val current = logbook ?: return
        saveChanges(
            current.copy(
                trainingSessions = current.trainingSessions.filterNot { it.id == id }
            )
        )
    }

    fun saveSettings(settings: UserPreferences) {
        val current = logbook ?: return
        saveChanges(
            current.copy(settings = settings)
        )
    }

    fun resetLogbook() {
        val current = logbook ?: return
        saveChanges(
            Logbook(
                selectedDateTime = current.selectedDateTime,
                profile = current.profile.copy(loggedAt = current.selectedDateTime),
                bodyHistory = emptyList(),
                settings = current.settings
            ).withPickedMoment(current.selectedDateTime)
        )
    }

    private fun saveChanges(nextLogbook: Logbook) {
        logbook = nextLogbook
        saveScope.launch(Dispatchers.IO) {
            saveMutex.withLock {
                repository.save(nextLogbook)
            }
        }
    }
}

@Composable
fun rememberAppState(repository: LogbookRepository): AppState {
    val saveScope = rememberCoroutineScope()
    return remember(repository, saveScope) {
        AppState(repository, saveScope)
    }
}
