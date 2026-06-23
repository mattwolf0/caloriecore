package app.caloriecore.data

import app.caloriecore.ui.model.FoodProduct
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class FoodFactsClient {
    suspend fun lookupBarcode(barcode: String): Result<FoodProduct> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val code = barcode.filter(Char::isDigit)
                require(code.isNotBlank()) { InvalidBarcode }

                val json = getJson(
                    "https://world.openfoodfacts.org/api/v3/product/$code" +
                        "?fields=code,product_name,brands,quantity,nutriments"
                )
                if (json.optString("status") != "success") {
                    error(NotFound)
                }

                parseFood(json.getJSONObject("product"), code)
            }
        }
    }

    suspend fun searchFoods(query: String): Result<List<FoodProduct>> {
        return withContext(Dispatchers.IO) {
            runCatching {
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
    }

    private fun searchFast(query: String): List<FoodProduct> {
        return runCatching {
            val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
            val json = getJson("https://search.openfoodfacts.org/search?q=$encoded&page_size=10")
            val hits = json.optJSONArray("hits")
                ?: json.optJSONArray("products")
                ?: JSONArray()

            hits.toFoods()
        }.getOrDefault(emptyList())
    }

    private fun searchLegacy(query: String): List<FoodProduct> {
        val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
        val json = getJson(
            "https://world.openfoodfacts.org/cgi/search.pl" +
                "?search_terms=$encoded&search_simple=1&action=process&json=1&page_size=10" +
                "&fields=code,product_name,brands,quantity,nutriments"
        )
        return (json.optJSONArray("products") ?: JSONArray()).toFoods()
    }

    private fun getJson(url: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("User-Agent", UserAgent)
            setRequestProperty("Accept", "application/json")
        }
        try {
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            check(body.isNotBlank()) { "empty_response" }
            return JSONObject(body)
        } finally {
            connection.disconnect()
        }
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
            kcalPer100g = kcalPer100g(nutriments).roundToInt(),
            proteinPer100g = nutriments.optDouble("proteins_100g", 0.0),
            carbsPer100g = nutriments.optDouble("carbohydrates_100g", 0.0),
            fatPer100g = nutriments.optDouble("fat_100g", 0.0),
            source = "openfoodfacts"
        )
    }

    private fun kcalPer100g(nutriments: JSONObject): Double {
        val kcal = nutriments.optDouble("energy-kcal_100g", Double.NaN)
        if (!kcal.isNaN()) return kcal

        val kj = nutriments.optDouble("energy-kj_100g", Double.NaN)
        return if (kj.isNaN()) 0.0 else kj / 4.184
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
        private const val UserAgent = "CalorieCore/0.1 Android"
    }
}
