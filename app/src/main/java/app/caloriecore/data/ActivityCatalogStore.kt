package app.caloriecore.data

import android.content.Context
import app.caloriecore.ui.model.ActivityCatalog
import app.caloriecore.ui.model.ActivityCatalogItem
import org.json.JSONObject

internal class ActivityCatalogStore(private val context: Context) {
    fun readCatalog(): ActivityCatalog {
        val jsonText = context.assets.open(FileName).bufferedReader().use { it.readText() }
        val root = JSONObject(jsonText)
        val jsonItems = root.getJSONArray("items")
        val items = buildList(jsonItems.length()) {
            for (index in 0 until jsonItems.length()) {
                val row = jsonItems.getJSONObject(index)
                add(
                    ActivityCatalogItem(
                        code = row.getString("code"),
                        categoryCode = row.getString("categoryCode"),
                        category = row.getString("category"),
                        nameEn = row.getString("nameEn"),
                        met = row.getDouble("met"),
                        common = row.optBoolean("common", false),
                        shortNameEn = row.optionalText("shortNameEn"),
                        nameHu = row.optionalText("nameHu"),
                        nameDe = row.optionalText("nameDe")
                    )
                )
            }
        }
        return ActivityCatalog(
            version = root.getString("version"),
            population = root.getString("population"),
            source = root.getString("source"),
            items = items
        )
    }

    private fun JSONObject.optionalText(name: String): String? =
        optString(name).trim().takeIf { it.isNotEmpty() }

    private companion object {
        const val FileName = "activity_catalog.json"
    }
}
