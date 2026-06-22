package app.caloriecore.data

import android.database.sqlite.SQLiteDatabase

internal fun createTables(db: SQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS settings (
            id INTEGER PRIMARY KEY CHECK (id = 1),
            language TEXT NOT NULL,
            theme_mode TEXT NOT NULL
        )
        """.trimIndent()
    )
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS profile_snapshots (
            id INTEGER PRIMARY KEY,
            logged_at INTEGER NOT NULL,
            user_id TEXT NOT NULL,
            sex TEXT NOT NULL,
            age INTEGER NOT NULL,
            height_cm INTEGER NOT NULL,
            weight_kg REAL NOT NULL,
            body_fat_percent REAL,
            sleep_hours REAL NOT NULL,
            watch_active_calories INTEGER NOT NULL,
            steps INTEGER NOT NULL,
            planned_workout_calories INTEGER NOT NULL,
            resting_heart_rate INTEGER NOT NULL
        )
        """.trimIndent()
    )
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS meals (
            id INTEGER PRIMARY KEY,
            logged_at INTEGER NOT NULL,
            name TEXT NOT NULL,
            barcode TEXT NOT NULL,
            serving_grams INTEGER NOT NULL,
            calories INTEGER NOT NULL,
            protein_grams REAL NOT NULL,
            carb_grams REAL NOT NULL,
            fat_grams REAL NOT NULL
        )
        """.trimIndent()
    )
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS food_products (
            id INTEGER PRIMARY KEY,
            code TEXT NOT NULL,
            name TEXT NOT NULL,
            serving_grams INTEGER NOT NULL,
            calories INTEGER NOT NULL,
            protein_grams REAL NOT NULL,
            carb_grams REAL NOT NULL,
            fat_grams REAL NOT NULL,
            source TEXT NOT NULL,
            updated_at INTEGER NOT NULL
        )
        """.trimIndent()
    )
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS workout_programs (
            id INTEGER PRIMARY KEY,
            title TEXT NOT NULL,
            focus TEXT NOT NULL,
            days_per_week INTEGER NOT NULL
        )
        """.trimIndent()
    )
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS program_exercises (
            id INTEGER PRIMARY KEY,
            program_id INTEGER NOT NULL,
            name TEXT NOT NULL,
            target TEXT NOT NULL,
            position INTEGER NOT NULL,
            FOREIGN KEY(program_id) REFERENCES workout_programs(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS program_exercise_targets (
            id INTEGER PRIMARY KEY,
            program_exercise_id INTEGER NOT NULL,
            set_count INTEGER NOT NULL,
            reps INTEGER NOT NULL,
            weight_kg REAL NOT NULL,
            position INTEGER NOT NULL,
            FOREIGN KEY(program_exercise_id) REFERENCES program_exercises(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS workout_sessions (
            id INTEGER PRIMARY KEY,
            program_id INTEGER,
            title TEXT NOT NULL,
            logged_at INTEGER NOT NULL,
            FOREIGN KEY(program_id) REFERENCES workout_programs(id) ON DELETE SET NULL
        )
        """.trimIndent()
    )
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS session_exercises (
            id INTEGER PRIMARY KEY,
            session_id INTEGER NOT NULL,
            name TEXT NOT NULL,
            target TEXT NOT NULL,
            position INTEGER NOT NULL,
            FOREIGN KEY(session_id) REFERENCES workout_sessions(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS exercise_sets (
            id INTEGER PRIMARY KEY,
            exercise_id INTEGER NOT NULL,
            logged_at INTEGER NOT NULL,
            reps INTEGER NOT NULL,
            weight_kg REAL NOT NULL,
            planned_reps INTEGER,
            planned_weight_kg REAL,
            position INTEGER NOT NULL,
            FOREIGN KEY(exercise_id) REFERENCES session_exercises(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )
}

internal fun dropTables(db: SQLiteDatabase) {
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
        "settings",
        "app_state",
        "app_log"
    ).forEach { table -> db.execSQL("DROP TABLE IF EXISTS $table") }
}
