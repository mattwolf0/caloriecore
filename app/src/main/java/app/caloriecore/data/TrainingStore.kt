package app.caloriecore.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import app.caloriecore.ui.model.LoggedExercise
import app.caloriecore.ui.model.LoggedSet
import app.caloriecore.ui.model.PlannedSetBlock
import app.caloriecore.ui.model.TrainingPlan
import app.caloriecore.ui.model.TrainingPlanExercise
import app.caloriecore.ui.model.TrainingSession

internal class TrainingStore(private val db: SQLiteDatabase) {
    fun readTrainingPlans(): List<TrainingPlan> = db.rawQuery(
        "SELECT * FROM workout_programs ORDER BY title COLLATE NOCASE",
        emptyArray()
    ).use { cursor ->
        cursor.mapRows {
            TrainingPlan(
                id = long("id"),
                title = string("title"),
                focus = string("focus"),
                daysPerWeek = int("days_per_week"),
                exercises = readTrainingPlanExercises(long("id"))
            )
        }
    }

    private fun readTrainingPlanExercises(programId: Long): List<TrainingPlanExercise> = db.rawQuery(
        "SELECT * FROM program_exercises WHERE program_id = ? ORDER BY position ASC, id ASC",
        arrayOf(programId.toString())
    ).use { cursor ->
        cursor.mapRows {
            TrainingPlanExercise(
                id = long("id"),
                name = string("name"),
                target = string("target"),
                position = int("position"),
                plannedSets = readTrainingPlanSetBlocks(long("id"))
            )
        }
    }

    private fun readTrainingPlanSetBlocks(planExerciseId: Long): List<PlannedSetBlock> = db.rawQuery(
        "SELECT * FROM program_exercise_targets WHERE program_exercise_id = ? ORDER BY position ASC, id ASC",
        arrayOf(planExerciseId.toString())
    ).use { cursor ->
        cursor.mapRows {
            PlannedSetBlock(
                id = long("id"),
                setCount = int("set_count"),
                reps = int("reps"),
                weightKg = double("weight_kg")
            )
        }
    }

    fun readTrainingSessionLogs(): List<TrainingSession> = db.rawQuery(
        "SELECT * FROM workout_sessions ORDER BY logged_at DESC",
        emptyArray()
    ).use { cursor ->
        cursor.mapRows {
            val sessionId = long("id")
            TrainingSession(
                id = sessionId,
                programId = nullableLong("program_id"),
                title = string("title"),
                loggedAt = long("logged_at"),
                exercises = readTrainingSessionExercises(sessionId)
            )
        }
    }

    private fun readTrainingSessionExercises(sessionId: Long): List<LoggedExercise> = db.rawQuery(
        "SELECT * FROM session_exercises WHERE session_id = ? ORDER BY position ASC, id ASC",
        arrayOf(sessionId.toString())
    ).use { cursor ->
        cursor.mapRows {
            val exerciseId = long("id")
            LoggedExercise(
                id = exerciseId,
                sessionId = sessionId,
                name = string("name"),
                target = string("target"),
                position = int("position"),
                sets = readLoggedExerciseSets(exerciseId)
            )
        }
    }

    private fun readLoggedExerciseSets(exerciseId: Long): List<LoggedSet> = db.rawQuery(
        "SELECT * FROM exercise_sets WHERE exercise_id = ? ORDER BY position ASC, id ASC",
        arrayOf(exerciseId.toString())
    ).use { cursor ->
        cursor.mapRows {
            LoggedSet(
                id = long("id"),
                loggedAt = long("logged_at"),
                reps = int("reps"),
                weightKg = double("weight_kg"),
                plannedReps = nullableInt("planned_reps"),
                plannedWeightKg = nullableDouble("planned_weight_kg")
            )
        }
    }

    fun insertTrainingPlan(plan: TrainingPlan) {
        val values = ContentValues().apply {
            put("id", plan.id)
            put("title", plan.title)
            put("focus", plan.focus)
            put("days_per_week", plan.daysPerWeek)
        }
        if (db.update("workout_programs", values, "id = ?", arrayOf(plan.id.toString())) == 0) {
            db.insertOrThrow("workout_programs", null, values)
        }
        db.delete("program_exercises", "program_id = ?", arrayOf(plan.id.toString()))
        plan.exercises.forEachIndexed { index, exercise ->
            val exerciseValues = ContentValues().apply {
                put("id", exercise.id)
                put("program_id", plan.id)
                put("name", exercise.name)
                put("target", exercise.target)
                put("position", index)
            }
            db.insertOrThrow("program_exercises", null, exerciseValues)
            exercise.plannedSets.forEachIndexed { targetIndex, target ->
                val targetValues = ContentValues().apply {
                    put("id", target.id)
                    put("program_exercise_id", exercise.id)
                    put("set_count", target.setCount)
                    put("reps", target.reps)
                    put("weight_kg", target.weightKg)
                    put("position", targetIndex)
                }
                db.insertOrThrow("program_exercise_targets", null, targetValues)
            }
        }
    }

    fun insertTrainingSessionLog(session: TrainingSession) {
        val values = ContentValues().apply {
            put("id", session.id)
            putNullable("program_id", session.programId)
            put("title", session.title)
            put("logged_at", session.loggedAt)
        }
        if (db.update("workout_sessions", values, "id = ?", arrayOf(session.id.toString())) == 0) {
            db.insertOrThrow("workout_sessions", null, values)
        }
        db.delete("session_exercises", "session_id = ?", arrayOf(session.id.toString()))
        session.exercises.forEachIndexed { exerciseIndex, exercise ->
            val exerciseValues = ContentValues().apply {
                put("id", exercise.id)
                put("session_id", session.id)
                put("name", exercise.name)
                put("target", exercise.target)
                put("position", exerciseIndex)
            }
            db.insertOrThrow("session_exercises", null, exerciseValues)
            exercise.sets.forEachIndexed { setIndex, set ->
                val setValues = ContentValues().apply {
                    put("id", set.id)
                    put("exercise_id", exercise.id)
                    put("logged_at", set.loggedAt)
                    put("reps", set.reps)
                    put("weight_kg", set.weightKg)
                    putNullable("planned_reps", set.plannedReps)
                    putNullable("planned_weight_kg", set.plannedWeightKg)
                    put("position", setIndex)
                }
                db.insertOrThrow("exercise_sets", null, setValues)
            }
        }
    }
}
