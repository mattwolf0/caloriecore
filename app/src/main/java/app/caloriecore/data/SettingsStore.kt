package app.caloriecore.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.model.UserPreferences

internal class SettingsStore(private val db: SQLiteDatabase) {
    fun readSettings(): UserPreferences = db.rawQuery(
        "SELECT language, theme_mode, profile_setup_complete FROM settings WHERE id = 1",
        emptyArray()
    ).use { cursor ->
        if (!cursor.moveToFirst()) return@use UserPreferences()
        UserPreferences(
            language = enumValue(cursor.string("language"), UiLanguage.System),
            themeMode = enumValue(cursor.string("theme_mode"), UiThemeMode.System),
            profileSetupComplete = cursor.int("profile_setup_complete") == 1
        )
    }

    fun insertSettings(settings: UserPreferences) {
        val values = ContentValues().apply {
            put("id", 1)
            put("language", settings.language.name)
            put("theme_mode", settings.themeMode.name)
            put("profile_setup_complete", if (settings.profileSetupComplete) 1 else 0)
        }
        db.insertWithOnConflict("settings", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
