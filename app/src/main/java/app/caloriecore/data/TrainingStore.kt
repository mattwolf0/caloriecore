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
        db.insertWithOnConflict("workout_programs", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        plan.exercises.forEachIndexed { index, exercise ->
            val exerciseValues = ContentValues().apply {
                put("id", exercise.id)
                put("program_id", plan.id)
                put("name", exercise.name)
                put("target", exercise.target)
                put("position", index)
            }
            db.insertWithOnConflict("program_exercises", null, exerciseValues, SQLiteDatabase.CONFLICT_REPLACE)
            exercise.plannedSets.forEachIndexed { targetIndex, target ->
                val targetValues = ContentValues().apply {
                    put("id", target.id)
                    put("program_exercise_id", exercise.id)
                    put("set_count", target.setCount)
                    put("reps", target.reps)
                    put("weight_kg", target.weightKg)
                    put("position", targetIndex)
                }
                db.insertWithOnConflict("program_exercise_targets", null, targetValues, SQLiteDatabase.CONFLICT_REPLACE)
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
        db.insertWithOnConflict("workout_sessions", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        session.exercises.forEachIndexed { exerciseIndex, exercise ->
            val exerciseValues = ContentValues().apply {
                put("id", exercise.id)
                put("session_id", session.id)
                put("name", exercise.name)
                put("target", exercise.target)
                put("position", exerciseIndex)
            }
            db.insertWithOnConflict("session_exercises", null, exerciseValues, SQLiteDatabase.CONFLICT_REPLACE)
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
                db.insertWithOnConflict("exercise_sets", null, setValues, SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
    }
}
