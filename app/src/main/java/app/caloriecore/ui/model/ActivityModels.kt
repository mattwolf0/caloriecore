package app.caloriecore.ui.model

import java.text.Normalizer
import java.util.Locale
import kotlin.math.roundToInt

enum class ActivitySource {
    Compendium,
    Manual
}

data class ActivityCatalogItem(
    val code: String,
    val categoryCode: String,
    val category: String,
    val nameEn: String,
    val met: Double,
    val common: Boolean = false,
    val shortNameEn: String? = null,
    val nameHu: String? = null,
    val nameDe: String? = null
) {
    fun nameFor(language: UiLanguage): String = when (language) {
        UiLanguage.Hungarian -> nameHu
        UiLanguage.German -> nameDe
        UiLanguage.System,
        UiLanguage.English -> shortNameEn
    } ?: shortNameEn ?: nameEn
}

data class ActivityCatalog(
    val version: String,
    val population: String,
    val source: String,
    val items: List<ActivityCatalogItem>
)

data class ActivityEntry(
    val id: Long = newLogId(),
    val loggedAt: Long = phoneNowMillis(),
    val catalogCode: String? = null,
    val name: String,
    val source: ActivitySource,
    val durationMinutes: Int? = null,
    val met: Double? = null,
    val weightKg: Double? = null,
    val activeCalories: Int
)

fun activeCaloriesFor(met: Double, weightKg: Double, minutes: Int): Int {
    if (!met.isFinite() || !weightKg.isFinite()) return 0
    if (weightKg <= 0.0 || minutes !in 1..1440) return 0
    val activeMet = (met - 1.0).coerceAtLeast(0.0)
    return (activeMet * 3.5 * weightKg / 200.0 * minutes).roundToInt()
}

fun ActivityEntry.canBeSaved(): Boolean {
    if (name.isBlank() || activeCalories <= 0) return false
    if (source == ActivitySource.Manual) return true
    return catalogCode != null &&
        durationMinutes in 1..1440 &&
        met?.let { it.isFinite() && it > 0.0 } == true &&
        weightKg?.let { it.isFinite() && it > 0.0 } == true
}

fun activitiesOnPhoneDay(entries: List<ActivityEntry>, selectedDateTime: Long): List<ActivityEntry> =
    entries.filter { samePhoneDay(it.loggedAt, selectedDateTime) }
        .sortedByDescending { it.loggedAt }

fun List<ActivityEntry>.activeCaloriesTotal(): Int = sumOf { it.activeCalories.coerceAtLeast(0) }

fun searchActivities(
    items: List<ActivityCatalogItem>,
    query: String,
    language: UiLanguage
): List<ActivityCatalogItem> {
    val cleanQuery = query.searchText()
    if (cleanQuery.isBlank()) {
        return items.filter { it.common }.sortedBy { it.nameFor(language).searchText() }
    }

    return items.mapNotNull { item ->
        val searchable = listOfNotNull(
            item.nameFor(language),
            item.shortNameEn,
            item.nameEn,
            item.nameHu,
            item.nameDe,
            item.category,
            item.code
        ).map { it.searchText() }
        val bestMatch = searchable.minOfOrNull { text ->
            when {
                text.startsWith(cleanQuery) -> 0
                text.contains(cleanQuery) -> 1
                else -> 2
            }
        } ?: 2
        if (bestMatch == 2) null else item to bestMatch
    }.sortedWith(
        compareBy<Pair<ActivityCatalogItem, Int>> { it.second }
            .thenBy { it.first.nameFor(language).searchText() }
    ).take(30).map { it.first }
}

private fun String.searchText(): String = Normalizer.normalize(trim(), Normalizer.Form.NFD)
    .replace("\\p{M}+".toRegex(), "")
    .lowercase(Locale.ROOT)
