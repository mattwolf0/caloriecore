package app.caloriecore.data

import android.database.sqlite.SQLiteDatabase
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.UserPreferences

internal fun upgradeDatabase(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    if (oldVersion < 2) {
        val legacyJsonPayload = LegacyLogbookImport(db).readPayload()
        dropTables(db)
        createTables(db)
        SettingsStore(db).insertSettings(UserPreferences())
        legacyJsonPayload?.let { LegacyLogbookImport(db).importPayload(it) }
        migrateToVersion4(db)
    } else {
        if (oldVersion < 3) migrateToVersion3(db)
        if (oldVersion < 4) migrateToVersion4(db)
    }
}

private fun migrateToVersion3(db: SQLiteDatabase) {
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
    runCatching { db.execSQL("ALTER TABLE exercise_sets ADD COLUMN planned_reps INTEGER") }
    runCatching { db.execSQL("ALTER TABLE exercise_sets ADD COLUMN planned_weight_kg REAL") }
}

private fun migrateToVersion4(db: SQLiteDatabase) {
    db.execSQL("UPDATE settings SET language = ? WHERE language = ?", arrayOf(UiLanguage.System.name, UiLanguage.English.name))
    db.execSQL("UPDATE settings SET theme_mode = ? WHERE theme_mode = ?", arrayOf(UiThemeMode.System.name, UiThemeMode.Light.name))
}
