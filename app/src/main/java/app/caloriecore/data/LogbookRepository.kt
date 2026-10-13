package app.caloriecore.data

import android.content.Context
import app.caloriecore.ui.model.ActivityCatalog
import app.caloriecore.ui.model.Logbook

interface LogbookStore {
    fun load(): Logbook
    fun save(logbook: Logbook)
    fun loadActivityCatalog(): ActivityCatalog
}

class LogbookRepository(context: Context) : LogbookStore {
    private val db = AppDatabase(context.applicationContext)
    private val activityCatalog = ActivityCatalogStore(context.applicationContext)

    override fun load(): Logbook = db.readLogbook()

    override fun save(logbook: Logbook) {
        db.saveLogbook(logbook)
    }

    override fun loadActivityCatalog(): ActivityCatalog = activityCatalog.readCatalog()

    fun close() {
        db.close()
    }
}
