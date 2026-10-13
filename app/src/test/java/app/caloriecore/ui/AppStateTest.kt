package app.caloriecore.ui

import app.caloriecore.data.LogbookStore
import app.caloriecore.ui.model.ActivityCatalog
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.Sex
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.model.phoneDayEndMillis
import app.caloriecore.ui.model.phoneNowMillis
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStateTest {
    @Test
    fun resetNeedsANewProfileAndClearsAllRows() = withState(FakeStore()) { state ->
        state.saveMeal(meal(1L))
        state.resetLogbook()
        waitForSave(state)
        assertFalse(state.logbook!!.settings.profileSetupComplete)
        assertTrue(state.logbook!!.bodyHistory.isEmpty())
        assertTrue(state.logbook!!.foodEntries.isEmpty())
    }

    @Test
    fun lateSavesCannotOverwriteNewerMeals() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val calls = AtomicInteger(0)
        val store = FakeStore()
        store.onSave = {
            if (calls.incrementAndGet() == 1) {
                started.countDown()
                check(release.await(5, TimeUnit.SECONDS))
            }
        }
        try {
            withState(store) { state ->
                state.saveMeal(meal(1L))
                assertTrue(started.await(5, TimeUnit.SECONDS))
                state.saveMeal(meal(2L))
                state.saveMeal(meal(3L))
                release.countDown()
                waitForSave(state)
                assertEquals(setOf(1L, 2L, 3L), store.saved.foodEntries.map { it.id }.toSet())
                assertEquals(2, calls.get())
            }
        } finally {
            release.countDown()
        }
    }

    @Test
    fun failedSaveCanBeRetriedWithoutLosingTheMeal() {
        val store = FakeStore().apply { failSave = true }
        withState(store) { state ->
            state.saveMeal(meal(1L))
            waitForSave(state)
            assertTrue(state.saveFailed)
            assertEquals(1, state.logbook!!.foodEntries.size)
            store.failSave = false
            state.retrySave()
            waitForSave(state)
            assertFalse(state.saveFailed)
            assertEquals(1, store.saved.foodEntries.size)
        }
    }

    @Test
    fun failedLoadCanBeRetried() {
        val store = FakeStore().apply { failLoad = true }
        withState(store) { state ->
            assertTrue(state.loadFailed)
            assertNull(state.logbook)
            store.failLoad = false
            state.load()
            assertFalse(state.loadFailed)
            assertNotNull(state.logbook)
        }
    }

    @Test
    fun keepsOneLoadWhenTheScreenIsRecreated() = runBlocking {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val calls = AtomicInteger(0)
        val store = FakeStore().apply {
            onLoad = {
                calls.incrementAndGet()
                entered.countDown()
                check(release.await(5, TimeUnit.SECONDS))
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val state = AppState(store, scope)
            val oldScreen = launch(start = CoroutineStart.UNDISPATCHED) { state.load() }
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            oldScreen.cancel()
            val newScreen = launch(start = CoroutineStart.UNDISPATCHED) { state.load() }
            release.countDown()
            withTimeout(5000) { newScreen.join() }
            assertEquals(1, calls.get())
            assertNotNull(state.logbook)
        } finally {
            release.countDown()
            scope.cancel()
        }
    }

    @Test
    fun returningToTheScreenDoesNotReloadTheSelectedDay() = withState(FakeStore()) { state ->
        val oldDay = phoneNowMillis() - 3 * 86_400_000L
        state.jumpToMoment(oldDay)
        state.load()
        assertEquals(oldDay, state.logbook!!.selectedDateTime)
        state.updateToday(phoneNowMillis() + 86_400_000L)
        assertEquals(oldDay, state.logbook!!.selectedDateTime)
    }

    @Test
    fun todayMovesForwardAfterMidnight() = withState(FakeStore()) { state ->
        val tomorrow = phoneDayEndMillis(state.logbook!!.selectedDateTime) + 1L
        state.updateToday(tomorrow)
        assertEquals(tomorrow, state.logbook!!.selectedDateTime)
        assertEquals(0, state.logbook!!.profile.steps)
    }

    @Test
    fun manualStepsAreNotOverwrittenByThePhone() {
        val store = FakeStore()
        store.saved = store.saved.copy(settings = store.saved.settings.copy(usePhoneSteps = false))
        withState(store) { state ->
            state.saveBodyEntry(state.logbook!!.profile.copy(steps = 1200))
            state.savePhoneSteps(3000)
            waitForSave(state)
            assertEquals(1200, state.logbook!!.profile.steps)
            assertEquals(1200, store.saved.profile.steps)
        }
    }

    @Test
    fun badValuesDoNotChangeTheLogbook() = withState(FakeStore()) { state ->
        state.saveMeal(meal(1L).copy(calories = -1))
        state.saveBodyEntry(state.logbook!!.profile.copy(heightCm = 0))
        assertTrue(state.logbook!!.foodEntries.isEmpty())
        assertEquals(165, state.logbook!!.profile.heightCm)
    }

    private fun meal(id: Long) = FoodEntry(
        id = id,
        name = "Bread",
        servingGrams = 100,
        calories = 120,
        proteinGrams = 10.0,
        carbGrams = 15.0,
        fatGrams = 2.0
    )

    private fun withState(store: FakeStore, block: suspend (AppState) -> Unit) = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val state = AppState(store, scope)
            state.load()
            block(state)
        } finally {
            scope.cancel()
        }
    }

    private suspend fun waitForSave(state: AppState) {
        withTimeout(5000) {
            while (state.isSaving) delay(10)
        }
    }

    private class FakeStore : LogbookStore {
        private val body = BodySnapshot(sex = Sex.Female, age = 41, heightCm = 165, weightKg = 63.0)
        var saved = Logbook(
            profile = body,
            bodyHistory = listOf(body),
            settings = UserPreferences(profileSetupComplete = true)
        )
        var failSave = false
        var failLoad = false
        var onSave: (() -> Unit)? = null
        var onLoad: (() -> Unit)? = null

        override fun load(): Logbook {
            onLoad?.invoke()
            check(!failLoad)
            return saved
        }

        override fun save(logbook: Logbook) {
            onSave?.invoke()
            check(!failSave)
            saved = logbook
        }

        override fun loadActivityCatalog() = ActivityCatalog("test", "test", "test", emptyList())
    }
}
