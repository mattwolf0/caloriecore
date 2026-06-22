package app.caloriecore

import android.content.Context

fun makeOldVersionThreeDatabase(context: Context) {
    val db = context.openOrCreateDatabase(DatabaseName, Context.MODE_PRIVATE, null)
    try {
        db.execSQL(
            """
            CREATE TABLE settings (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                language TEXT NOT NULL,
                theme_mode TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("INSERT INTO settings (id, language, theme_mode) VALUES (1, 'English', 'Light')")
        db.execSQL(
            """
            CREATE TABLE profile_snapshots (
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
            CREATE TABLE meals (
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
            CREATE TABLE food_products (
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
            CREATE TABLE workout_programs (
                id INTEGER PRIMARY KEY,
                title TEXT NOT NULL,
                focus TEXT NOT NULL,
                days_per_week INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE program_exercises (
                id INTEGER PRIMARY KEY,
                program_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                target TEXT NOT NULL,
                position INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE program_exercise_targets (
                id INTEGER PRIMARY KEY,
                program_exercise_id INTEGER NOT NULL,
                set_count INTEGER NOT NULL,
                reps INTEGER NOT NULL,
                weight_kg REAL NOT NULL,
                position INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE workout_sessions (
                id INTEGER PRIMARY KEY,
                program_id INTEGER,
                title TEXT NOT NULL,
                logged_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE session_exercises (
                id INTEGER PRIMARY KEY,
                session_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                target TEXT NOT NULL,
                position INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE exercise_sets (
                id INTEGER PRIMARY KEY,
                exercise_id INTEGER NOT NULL,
                logged_at INTEGER NOT NULL,
                reps INTEGER NOT NULL,
                weight_kg REAL NOT NULL,
                planned_reps INTEGER,
                planned_weight_kg REAL,
                position INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE app_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created_at INTEGER NOT NULL,
                level TEXT NOT NULL,
                tag TEXT NOT NULL,
                message TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.version = 3
    } finally {
        db.close()
    }
}

const val DatabaseName = "calorie_core.db"
