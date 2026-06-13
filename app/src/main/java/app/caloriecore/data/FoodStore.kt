package app.caloriecore.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import app.caloriecore.ui.model.FoodEntry

internal class FoodStore(private val db: SQLiteDatabase) {
    fun readFoodEntries(): List<FoodEntry> = db.rawQuery(
        "SELECT * FROM meals ORDER BY logged_at DESC",
        emptyArray()
    ).use { cursor ->
        cursor.mapRows {
            FoodEntry(
                id = long("id"),
                loggedAt = long("logged_at"),
                name = string("name"),
                barcode = string("barcode"),
                servingGrams = int("serving_grams"),
                calories = int("calories"),
                proteinGrams = double("protein_grams"),
                carbGrams = double("carb_grams"),
                fatGrams = double("fat_grams")
            )
        }
    }

    fun insertFoodEntry(meal: FoodEntry) {
        val values = ContentValues().apply {
            put("id", meal.id)
            put("logged_at", meal.loggedAt)
            put("name", meal.name)
            put("barcode", meal.barcode)
            put("serving_grams", meal.servingGrams)
            put("calories", meal.calories)
            put("protein_grams", meal.proteinGrams)
            put("carb_grams", meal.carbGrams)
            put("fat_grams", meal.fatGrams)
        }
        db.insertWithOnConflict("meals", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
