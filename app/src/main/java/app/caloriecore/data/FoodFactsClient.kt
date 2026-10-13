package app.caloriecore.data

import app.caloriecore.ui.model.FoodProduct
import app.caloriecore.ui.model.withMissingMacrosFrom
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class FoodFactsClient internal constructor(
    private val readJson: (String) -> JSONObject,
    private val barcodeFallback: suspend (String) -> FoodProduct
) {
    constructor() : this(::readFoodJson, CalorieApiClient()::lookupBarcode)

    suspend fun lookupBarcode(barcode: String): Result<FoodProduct> = withContext(Dispatchers.IO) {
        foodLookupResult {
            val code = barcode.filter(Char::isDigit)
            require(code.isNotBlank()) { InvalidBarcode }
            lookupFood(code)
        }
    }

    private suspend fun lookupFood(code: String): FoodProduct {
        val openFood = try {
            lookupOpenFoodFacts(code)
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            if (error.message != NotFound) throw error
            null
        }

        if (openFood?.hasCompleteMacros == true) return openFood

        val fallback = try {
            barcodeFallback(code)
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            if (openFood != null) return openFood
            throw error
        }

        return openFood?.withMissingMacrosFrom(fallback) ?: fallback
    }

    private fun lookupOpenFoodFacts(code: String): FoodProduct {
        val json = readJson(
            "https://world.openfoodfacts.org/api/v3/product/$code" +
                "?fields=code,product_name,brands,quantity,nutriments"
        )
        if (json.optString("status") != "success") {
            error(NotFound)
        }

        return parseFood(json.getJSONObject("product"), code)
    }

    suspend fun searchFoods(query: String): Result<List<FoodProduct>> = withContext(Dispatchers.IO) {
        foodLookupResult {
            val text = query.trim()
            require(text.length >= 2) { SearchTooShort }

            val fastResults = searchFast(text)
            if (fastResults.isNotEmpty()) {
                fastResults
            } else {
                searchLegacy(text)
            }
        }
    }

    private fun searchFast(query: String): List<FoodProduct> = foodLookupResult {
        val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
        val json = readJson("https://search.openfoodfacts.org/search?q=$encoded&page_size=10")
        val hits = json.optJSONArray("hits")
            ?: json.optJSONArray("products")
            ?: JSONArray()

        hits.toFoods()
    }.getOrDefault(emptyList())

    private fun searchLegacy(query: String): List<FoodProduct> {
        val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
        val json = readJson(
            "https://world.openfoodfacts.org/cgi/search.pl" +
                "?search_terms=$encoded&search_simple=1&action=process&json=1&page_size=10" +
                "&fields=code,product_name,brands,quantity,nutriments"
        )
        return (json.optJSONArray("products") ?: JSONArray()).toFoods()
    }

    private fun JSONArray.toFoods(): List<FoodProduct> {
        val foods = mutableListOf<FoodProduct>()
        for (index in 0 until length()) {
            val hit = getJSONObject(index)
            val product = hit.optJSONObject("product")
                ?: hit.optJSONObject("_source")
                ?: hit
            val code = product.optString("code").ifBlank {
                hit.optString("code")
            }

            val food = parseFood(product, code)
            if (food.name.isNotBlank()) {
                foods += food
            }
        }

        return foods
            .distinctBy { food -> food.code.ifBlank { food.name } }
            .take(10)
    }

    private fun parseFood(product: JSONObject, fallbackCode: String): FoodProduct {
        val nutriments = product.optJSONObject("nutriments") ?: JSONObject()
        val name = product.optString("product_name").ifBlank {
            product.optString("brands").ifBlank { "Unknown product" }
        }
        return FoodProduct(
            code = product.optString("code").ifBlank { fallbackCode },
            name = name,
            servingGrams = servingFromQuantity(product.optString("quantity")),
            kcalPer100g = kcalPer100g(nutriments)?.roundToInt(),
            proteinPer100g = nutriments.nonNegativeDoubleOrNull("proteins_100g"),
            carbsPer100g = nutriments.nonNegativeDoubleOrNull("carbohydrates_100g"),
            fatPer100g = nutriments.nonNegativeDoubleOrNull("fat_100g"),
            source = "openfoodfacts"
        )
    }

    private fun kcalPer100g(nutriments: JSONObject): Double? {
        nutriments.nonNegativeDoubleOrNull("energy-kcal_100g")?.let { return it }

        return nutriments.nonNegativeDoubleOrNull("energy-kj_100g")?.div(4.184)
    }

    private fun JSONObject.nonNegativeDoubleOrNull(key: String): Double? {
        if (!has(key) || isNull(key)) return null
        return optDouble(key, Double.NaN).takeIf { it.isFinite() && it >= 0.0 }
    }

    private fun servingFromQuantity(quantity: String): Int {
        val amount = Regex("\\d+(?:[.,]\\d+)?")
            .find(quantity)
            ?.value
            ?.replace(',', '.')
            ?.toDoubleOrNull()
            ?: return 100
        return amount.roundToInt().coerceIn(1, 1500)
    }

    companion object {
        const val InvalidBarcode = "invalid_barcode"
        const val SearchTooShort = "invalid_search_query"
        const val NotFound = "product_not_found"
    }
}

internal fun readFoodJson(url: String): JSONObject {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 8000
        readTimeout = 8000
        setRequestProperty("User-Agent", "CalorieCore/0.1 Android")
        setRequestProperty("Accept", "application/json")
    }
    try {
        if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            error(FoodFactsClient.NotFound)
        }
        check(connection.responseCode in 200..299) { "food_lookup_failed" }
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        check(body.isNotBlank()) { "empty_response" }
        return JSONObject(body)
    } finally {
        connection.disconnect()
    }
}

private inline fun <T> foodLookupResult(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    Result.failure(error)
}
