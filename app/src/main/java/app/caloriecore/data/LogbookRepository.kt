package app.caloriecore.data

import android.content.Context
import app.caloriecore.ui.model.Logbook

class LogbookRepository(context: Context) {
    private val db = AppDatabase(context.applicationContext)

    fun load(): Logbook {
        return db.readLogbook()
    }

    fun save(logbook: Logbook) {
        db.saveLogbook(logbook)
    }
}
