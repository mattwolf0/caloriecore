package app.caloriecore.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.caloriecore.data.LogbookRepository
import app.caloriecore.data.LogbookStore
import app.caloriecore.ui.model.ActivityCatalog
import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.model.canBeSaved
import app.caloriecore.ui.model.phoneNowMillis
import app.caloriecore.ui.model.putById
import app.caloriecore.ui.model.samePhoneDay
import app.caloriecore.ui.model.withBodyCheckIn
import app.caloriecore.ui.model.withPickedMoment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppState internal constructor(private val repository: LogbookStore, private val saveScope: CoroutineScope) {
    private val pendingSaves = Channel<Pair<Long, Logbook>>(Channel.CONFLATED)
    private var saveNumber = 0L
    private var loadJob: Deferred<Pair<Logbook, ActivityCatalog>>? = null

    var isSaving by mutableStateOf(false)
        private set

    var saveFailed by mutableStateOf(false)
        private set
    var loadFailed by mutableStateOf(false)
        private set
    var followsToday by mutableStateOf(true)
        private set

    init {
        saveScope.launch {
            for ((number, nextLogbook) in pendingSaves) {
                try {
                    withContext(Dispatchers.IO) { repository.save(nextLogbook) }
                    if (number == saveNumber) saveFailed = false
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    if (number == saveNumber) saveFailed = true
                } finally {
                    if (number == saveNumber) isSaving = false
                }
            }
        }
    }

    var logbook by mutableStateOf<Logbook?>(null)
        private set

    var activityCatalog by mutableStateOf<ActivityCatalog?>(null)
        private set

    suspend fun load(pickedMoment: Long? = null, followToday: Boolean = true) {
        if (logbook != null) return
        loadFailed = false
        val job = loadJob ?: saveScope.async(Dispatchers.IO) {
            repository.load() to repository.loadActivityCatalog()
        }.also { loadJob = it }
        try {
            val loaded = job.await()
            if (logbook != null) return
            activityCatalog = loaded.second
            followsToday = followToday
            logbook = if (pickedMoment != null && !followToday) {
                loaded.first.withPickedMoment(pickedMoment)
            } else {
                val now = phoneNowMillis()
                if (samePhoneDay(loaded.first.selectedDateTime, now)) loaded.first else loaded.first.withPickedMoment(now)
            }
            loadJob = null
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (loadJob === job) loadJob = null
            loadFailed = true
        }
    }

    fun jumpToMoment(nextDateTime: Long) {
        followsToday = samePhoneDay(nextDateTime, phoneNowMillis())
        logbook = logbook?.withPickedMoment(nextDateTime)
    }

    fun updateToday(now: Long = phoneNowMillis()) {
        val current = logbook ?: return
        if (followsToday && !samePhoneDay(current.selectedDateTime, now)) {
            logbook = current.withPickedMoment(now)
        }
    }

    fun saveBodyEntry(profile: BodySnapshot) {
        if (!profile.canBeSaved()) return
        val current = logbook ?: return
        val savedProfile = if (current.settings.usePhoneSteps && samePhoneDay(current.selectedDateTime, phoneNowMillis())) {
            profile.copy(steps = current.profile.steps)
        } else {
            profile
        }
        saveChanges(current.withBodyCheckIn(savedProfile))
    }

    fun savePhoneSteps(steps: Int) {
        val current = logbook ?: return
        if (!current.settings.usePhoneSteps || !samePhoneDay(current.selectedDateTime, phoneNowMillis())) return
        saveChanges(current.withBodyCheckIn(current.profile.copy(steps = steps)))
    }

    fun completeProfileSetup(profile: BodySnapshot) {
        if (!profile.canBeSaved()) return
        val current = logbook ?: return
        val savedProfile = profile.copy(loggedAt = current.selectedDateTime)
        saveChanges(
            current.copy(
                profile = savedProfile,
                bodyHistory = listOf(savedProfile),
                settings = current.settings.copy(profileSetupComplete = true)
            )
        )
    }

    fun saveMeal(meal: FoodEntry) {
        if (!meal.canBeSaved()) return
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

    fun saveActivity(entry: ActivityEntry) {
        if (!entry.canBeSaved()) return
        val current = logbook ?: return
        saveChanges(
            current.copy(
                activityEntries = current.activityEntries
                    .putById(entry.id, entry) { it.id }
                    .sortedByDescending { it.loggedAt }
            )
        )
    }

    fun deleteActivity(id: Long) {
        val current = logbook ?: return
        saveChanges(
            current.copy(
                activityEntries = current.activityEntries.filterNot { it.id == id }
            )
        )
    }

    fun savePlan(plan: TrainingPlan) {
        if (!plan.canBeSaved()) return
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
        if (!session.canBeSaved()) return
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
        followsToday = true
        saveChanges(
            Logbook(
                bodyHistory = emptyList(),
                settings = current.settings.copy(profileSetupComplete = false)
            )
        )
    }

    private fun saveChanges(nextLogbook: Logbook) {
        logbook = nextLogbook
        saveNumber++
        isSaving = true
        pendingSaves.trySend(saveNumber to nextLogbook)
    }

    fun retrySave() {
        logbook?.let { saveChanges(it) }
    }
}

@Composable
fun rememberAppState(context: Context): AppState {
    val factory = remember(context) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AppViewModel(LogbookRepository(context.applicationContext)) as T
            }
        }
    }
    return viewModel<AppViewModel>(factory = factory).state
}

internal class AppViewModel(private val repository: LogbookRepository) : ViewModel() {
    val state = AppState(repository, viewModelScope)

    override fun onCleared() {
        repository.close()
    }
}
