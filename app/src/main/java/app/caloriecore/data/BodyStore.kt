package app.caloriecore.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.Sex

internal class BodyStore(private val db: SQLiteDatabase) {
    fun readBodySnapshots(): List<BodySnapshot> = db.rawQuery(
        "SELECT * FROM profile_snapshots ORDER BY logged_at DESC",
        emptyArray()
    ).use { cursor ->
        cursor.mapRows {
            BodySnapshot(
                id = long("id"),
                loggedAt = long("logged_at"),
                userId = string("user_id"),
                sex = enumValue(string("sex"), Sex.Male),
                age = int("age"),
                heightCm = int("height_cm"),
                weightKg = double("weight_kg"),
                bodyFatPercent = nullableDouble("body_fat_percent"),
                sleepHours = double("sleep_hours"),
                watchActiveCalories = int("watch_active_calories"),
                steps = int("steps"),
                plannedWorkoutCalories = int("planned_workout_calories"),
                restingHeartRate = int("resting_heart_rate")
            )
        }
    }

    fun insertBodySnapshot(profile: BodySnapshot) {
        val values = ContentValues().apply {
            put("id", profile.id)
            put("logged_at", profile.loggedAt)
            put("user_id", profile.userId)
            put("sex", profile.sex.name)
            put("age", profile.age)
            put("height_cm", profile.heightCm)
            put("weight_kg", profile.weightKg)
            putNullable("body_fat_percent", profile.bodyFatPercent)
            put("sleep_hours", profile.sleepHours)
            put("watch_active_calories", profile.watchActiveCalories)
            put("steps", profile.steps)
            put("planned_workout_calories", profile.plannedWorkoutCalories)
            put("resting_heart_rate", profile.restingHeartRate)
        }
        db.insertWithOnConflict("profile_snapshots", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
