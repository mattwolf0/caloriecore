package app.caloriecore.data

import app.caloriecore.ui.model.FoodProduct
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import org.json.JSONObject

internal class CalorieApiClient {
    private val cache = mutableMapOf<String, FoodProduct>()
    private var lastRequestTime = 0L

    suspend fun lookupBarcode(barcode: String): FoodProduct {
        cache[barcode]?.let { return it }

        val elapsed = System.currentTimeMillis() - lastRequestTime
        if (elapsed < RequestDelay) {
            delay(RequestDelay - elapsed)
        }

        lastRequestTime = System.currentTimeMillis()
        val food = fetchBarcode(barcode)
        cache[barcode] = food
        return food
    }

    private fun fetchBarcode(barcode: String): FoodProduct {
        val connection = (URL("$BarcodeUrl/$barcode").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("User-Agent", UserAgent)
            setRequestProperty("Accept", "application/json")
        }
        try {
            return when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    parseFood(JSONObject(body), barcode)
                }
                HttpURLConnection.HTTP_NOT_FOUND -> error(FoodFactsClient.NotFound)
                TooManyRequests -> error(RateLimited)
                else -> error(Unavailable)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseFood(json: JSONObject, fallbackCode: String): FoodProduct {
        val product = json.optJSONObject("product") ?: JSONObject()
        val nutrition = json.optJSONObject("nutrition_per_100g") ?: JSONObject()
        val name = product.optString("name").ifBlank {
            product.optString("brand").ifBlank { "Unknown product" }
        }

        return FoodProduct(
            code = json.optString("barcode").ifBlank { fallbackCode },
            name = name,
            servingGrams = 100,
            kcalPer100g = nutrition.numberOrNull("energy_kcal")?.roundToInt(),
            proteinPer100g = nutrition.numberOrNull("protein_g"),
            carbsPer100g = nutrition.numberOrNull("carbohydrates_g"),
            fatPer100g = nutrition.numberOrNull("fat_g"),
            source = "calorieapi"
        )
    }

    private fun JSONObject.numberOrNull(key: String): Double? {
        if (!has(key) || isNull(key)) return null
        val number = optDouble(key, Double.NaN)
        return if (number.isFinite() && number >= 0.0) number else null
    }

    private companion object {
        const val BarcodeUrl = "https://calorieapiadmin.com/api/v1/public/search/barcode"
        const val RequestDelay = 1100L
        const val TooManyRequests = 429
        const val RateLimited = "calorie_api_rate_limited"
        const val Unavailable = "calorie_api_unavailable"
        const val UserAgent = "CalorieCore/0.1 Android"
    }
}
