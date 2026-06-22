package app.caloriecore.data

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import app.caloriecore.ui.model.BodySnapshot
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.LoggedExercise
import app.caloriecore.ui.model.LoggedSet
import app.caloriecore.ui.model.Sex
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingPlanExercise
import app.caloriecore.ui.model.TrainingSession
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.UserPreferences
import app.caloriecore.ui.model.newLogId
import app.caloriecore.ui.model.phoneNowMillis
import org.json.JSONObject

internal class LegacyLogbookImport(private val db: SQLiteDatabase) {
    fun readPayload(): String? = runCatching {
        db.rawQuery("SELECT payload FROM app_state WHERE id = 1", emptyArray()).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()

    fun importPayload(payload: String) {
        runCatching {
            val settingsStore = SettingsStore(db)
            val bodyStore = BodyStore(db)
            val foodStore = FoodStore(db)
            val trainingStore = TrainingStore(db)
            val json = JSONObject(payload)

            json.optJSONObject("settings")?.let { settings ->
                settingsStore.insertSettings(
                    UserPreferences(
                        language = enumValue(settings.optString("language"), UiLanguage.System),
                        themeMode = enumValue(settings.optString("themeMode"), UiThemeMode.System)
                    )
                )
            }
            val migratedAt = phoneNowMillis()
            json.optJSONObject("profile")?.let { profile ->
                bodyStore.insertBodySnapshot(
                    BodySnapshot(
                        id = profile.optLong("id", newLogId()),
                        loggedAt = profile.optLong("loggedAt", migratedAt),
                        userId = profile.optString("userId", "local-user"),
                        sex = enumValue(profile.optString("sex"), Sex.Male),
                        age = profile.optInt("age", 29),
                        heightCm = profile.optInt("heightCm", 178),
                        weightKg = profile.optDouble("weightKg", 82.0),
                        bodyFatPercent = profile.optionalDouble("bodyFatPercent"),
                        sleepHours = profile.optDouble("sleepHours", 7.2),
                        watchActiveCalories = profile.optInt("watchActiveCalories", 0),
                        steps = profile.optInt("steps", 0),
                        plannedWorkoutCalories = profile.optInt("plannedWorkoutCalories", 0),
                        restingHeartRate = profile.optInt("restingHeartRate", 60)
                    )
                )
            }
            json.optJSONArray("meals")?.let { meals ->
                for (index in 0 until meals.length()) {
                    val meal = meals.getJSONObject(index)
                    foodStore.insertFoodEntry(
                        FoodEntry(
                            id = meal.optLong("id", newLogId()),
                            loggedAt = meal.optLong("loggedAt", migratedAt),
                            name = meal.optString("name"),
                            barcode = meal.optString("barcode"),
                            servingGrams = meal.optInt("servingGrams", 100),
                            calories = meal.optInt("calories", 0),
                            proteinGrams = meal.optDouble("proteinGrams", 0.0),
                            carbGrams = meal.optDouble("carbGrams", 0.0),
                            fatGrams = meal.optDouble("fatGrams", 0.0)
                        )
                    )
                }
            }
            json.optJSONArray("programs")?.let { plans ->
                for (index in 0 until plans.length()) {
                    val legacyPlan = plans.getJSONObject(index)
                    val title = legacyPlan.optString("title")
                    // Do not keep the old default workout plans.
                    if (title !in LegacySeededProgramTitles && title.isNotBlank()) {
                        val id = legacyPlan.optLong("id", newLogId())
                        val exercises = mutableListOf<TrainingPlanExercise>()
                        val names = legacyPlan.optJSONArray("exercises")
                        if (names != null) {
                            for (exerciseIndex in 0 until names.length()) {
                                exercises += TrainingPlanExercise(
                                    name = names.getString(exerciseIndex),
                                    position = exerciseIndex
                                )
                            }
                        }
                        trainingStore.insertTrainingPlan(
                            TrainingPlan(
                                id = id,
                                title = title,
                                focus = legacyPlan.optString("focus"),
                                daysPerWeek = legacyPlan.optInt("daysPerWeek", 3),
                                exercises = exercises
                            )
                        )
                    }
                }
            }
            val legacyExercises = json.optJSONArray("exercises")
            if (legacyExercises != null && legacyExercises.length() > 0) {
                val sessionId = newLogId()
                val sessionExercises = mutableListOf<LoggedExercise>()
                for (index in 0 until legacyExercises.length()) {
                    val exercise = legacyExercises.getJSONObject(index)
                    val exerciseId = exercise.optLong("id", newLogId())
                    val sets = mutableListOf<LoggedSet>()
                    exercise.optJSONArray("sets")?.let { legacySets ->
                        for (setIndex in 0 until legacySets.length()) {
                            val set = legacySets.getJSONObject(setIndex)
                            sets += LoggedSet(
                                id = newLogId(),
                                loggedAt = migratedAt,
                                reps = set.optInt("reps"),
                                weightKg = set.optDouble("weightKg")
                            )
                        }
                    }
                    sessionExercises += LoggedExercise(
                        id = exerciseId,
                        sessionId = sessionId,
                        name = exercise.optString("name"),
                        target = exercise.optString("target"),
                        position = index,
                        sets = sets
                    )
                }
                trainingStore.insertTrainingSessionLog(
                    TrainingSession(
                        id = sessionId,
                        title = "Migrated workout",
                        loggedAt = migratedAt,
                        exercises = sessionExercises
                    )
                )
            }
        }.onFailure { error ->
            Log.e(DbTag, "Legacy migration failed: ${error.message}", error)
        }
    }

    private companion object {
        val LegacySeededProgramTitles = setOf("Push Pull Legs", "Full Body 3", "Upper Lower")
    }
}

private fun JSONObject.optionalDouble(name: String): Double? =
    if (has(name) && !isNull(name)) optDouble(name) else null
