package app.caloriecore.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import app.caloriecore.ui.model.ActivityEntry
import app.caloriecore.ui.model.ActivitySource

internal class ActivityStore(private val db: SQLiteDatabase) {
    fun readEntries(): List<ActivityEntry> = db.rawQuery(
        "SELECT * FROM activity_entries ORDER BY logged_at DESC",
        emptyArray()
    ).use { cursor ->
        cursor.mapRows {
            ActivityEntry(
                id = long("id"),
                loggedAt = long("logged_at"),
                catalogCode = nullableString("catalog_code"),
                name = string("name"),
                source = enumValue(string("source"), ActivitySource.Manual),
                durationMinutes = nullableInt("duration_minutes"),
                met = nullableDouble("met"),
                weightKg = nullableDouble("weight_kg"),
                activeCalories = int("active_calories")
            )
        }
    }

    fun insertEntry(entry: ActivityEntry) {
        val values = ContentValues().apply {
            put("id", entry.id)
            put("logged_at", entry.loggedAt)
            putNullable("catalog_code", entry.catalogCode)
            put("name", entry.name)
            put("source", entry.source.name)
            putNullable("duration_minutes", entry.durationMinutes)
            putNullable("met", entry.met)
            putNullable("weight_kg", entry.weightKg)
            put("active_calories", entry.activeCalories)
        }
        db.insertWithOnConflict("activity_entries", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
