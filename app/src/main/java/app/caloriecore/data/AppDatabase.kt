package app.caloriecore.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.model.nearestBodyCheckIn
import app.caloriecore.ui.model.phoneNowMillis

internal const val DbName = "calorie_core.db"
internal const val DbVersion = 8
internal const val DbTag = "AppDb"

internal class AppDatabase(context: Context) : SQLiteOpenHelper(context, DbName, null, DbVersion) {

    private var savedLogbook: Logbook? = null

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        SettingsStore(db).insertSettings(UserPreferences())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        upgradeDatabase(db, oldVersion, newVersion)
    }

    fun readLogbook(): Logbook {
        val selectedDateTime = phoneNowMillis()
        val db = readableDatabase
        val bodySnapshots = BodyStore(db).readBodySnapshots()
        val selectedBodySnapshot = nearestBodyCheckIn(bodySnapshots, selectedDateTime)
        val logbook = Logbook(
            selectedDateTime = selectedDateTime,
            profile = selectedBodySnapshot,
            bodyHistory = bodySnapshots,
            foodEntries = FoodStore(db).readFoodEntries(),
            activityEntries = ActivityStore(db).readEntries(),
            trainingSessions = TrainingStore(db).readTrainingSessionLogs(),
            trainingPlans = TrainingStore(db).readTrainingPlans(),
            settings = SettingsStore(db).readSettings()
        )
        savedLogbook = logbook
        return logbook
    }

    fun saveLogbook(logbook: Logbook) {
        val previous = savedLogbook ?: readLogbook()
        val oldBody = previous.bodyHistory.associateBy { it.id }
        val oldMeals = previous.foodEntries.associateBy { it.id }
        val oldActivities = previous.activityEntries.associateBy { it.id }
        val oldPlans = previous.trainingPlans.associateBy { it.id }
        val oldSessions = previous.trainingSessions.associateBy { it.id }
        writableDatabase.transaction {
            if (previous.settings != logbook.settings) SettingsStore(this).insertSettings(logbook.settings)
            val bodyStore = BodyStore(this)
            val foodStore = FoodStore(this)
            val activityStore = ActivityStore(this)
            val trainingStore = TrainingStore(this)

            deleteMissingRows(this, "profile_snapshots", previous.bodyHistory.map { it.id }, logbook.bodyHistory.map { it.id })
            deleteMissingRows(this, "meals", previous.foodEntries.map { it.id }, logbook.foodEntries.map { it.id })
            deleteMissingRows(this, "activity_entries", previous.activityEntries.map { it.id }, logbook.activityEntries.map { it.id })
            deleteMissingRows(this, "workout_sessions", previous.trainingSessions.map { it.id }, logbook.trainingSessions.map { it.id })
            deleteMissingRows(this, "workout_programs", previous.trainingPlans.map { it.id }, logbook.trainingPlans.map { it.id })

            logbook.bodyHistory.filter { oldBody[it.id] != it }.forEach { bodyStore.insertBodySnapshot(it) }
            logbook.foodEntries.filter { oldMeals[it.id] != it }.forEach { foodStore.insertFoodEntry(it) }
            logbook.activityEntries.filter { oldActivities[it.id] != it }.forEach { activityStore.insertEntry(it) }
            logbook.trainingPlans.filter { oldPlans[it.id] != it }.forEach { trainingStore.insertTrainingPlan(it) }
            logbook.trainingSessions.filter { oldSessions[it.id] != it }.forEach { trainingStore.insertTrainingSessionLog(it) }
            if (!logbook.settings.profileSetupComplete && logbook.foodEntries.isEmpty()) {
                delete("food_products", null, null)
            }
        }
        savedLogbook = logbook
    }

    private fun deleteMissingRows(db: SQLiteDatabase, table: String, oldIds: List<Long>, newIds: List<Long>) {
        val keptIds = newIds.toSet()
        oldIds.filterNot { it in keptIds }.forEach { id ->
            db.delete(table, "id = ?", arrayOf(id.toString()))
        }
    }
}
