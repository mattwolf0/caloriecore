package app.caloriecore.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.model.nearestBodyCheckIn
import app.caloriecore.ui.model.phoneNowMillis

internal const val DbName = "calorie_core.db"
internal const val DbVersion = 4
internal const val DbTag = "AppDb"

internal class AppDatabase(context: Context) :
    SQLiteOpenHelper(context, DbName, null, DbVersion) {

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
        val bodySnapshots = BodyStore(db).readBodySnapshots().ifEmpty { listOf(BodySnapshot(loggedAt = selectedDateTime)) }
        val selectedBodySnapshot = nearestBodyCheckIn(bodySnapshots, selectedDateTime)
        return Logbook(
            selectedDateTime = selectedDateTime,
            profile = selectedBodySnapshot,
            bodyHistory = bodySnapshots,
            foodEntries = FoodStore(db).readFoodEntries(),
            trainingSessions = TrainingStore(db).readTrainingSessionLogs(),
            trainingPlans = TrainingStore(db).readTrainingPlans(),
            settings = SettingsStore(db).readSettings()
        )
    }

    fun saveLogbook(logbook: Logbook) {
        // Small local dataset, so a full rewrite is fine for now.
        writableDatabase.transaction {
            clearUserData(this)
            SettingsStore(this).insertSettings(logbook.settings)
            val bodyStore = BodyStore(this)
            val foodStore = FoodStore(this)
            val trainingStore = TrainingStore(this)

            logbook.bodyHistory.forEach { bodyStore.insertBodySnapshot(it) }
            logbook.foodEntries.forEach { foodStore.insertFoodEntry(it) }
            logbook.trainingPlans.forEach { trainingStore.insertTrainingPlan(it) }
            logbook.trainingSessions.forEach { trainingStore.insertTrainingSessionLog(it) }
        }
    }

    private fun clearUserData(db: SQLiteDatabase) {
        listOf(
            "exercise_sets",
            "session_exercises",
            "workout_sessions",
            "program_exercise_targets",
            "program_exercises",
            "workout_programs",
            "food_products",
            "meals",
            "profile_snapshots",
            "settings"
        ).forEach { table -> db.delete(table, null, null) }
    }
}
