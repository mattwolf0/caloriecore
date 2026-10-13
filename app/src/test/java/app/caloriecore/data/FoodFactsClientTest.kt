package app.caloriecore.data

import app.caloriecore.ui.model.FoodProduct
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodFactsClientTest {
    @Test
    fun httpNotFoundUsesTheBarcodeFallback() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/missing") { exchange ->
            exchange.sendResponseHeaders(404, -1)
            exchange.close()
        }
        server.start()
        try {
            var fallbackCode = ""
            val client = FoodFactsClient(
                readJson = { readFoodJson("http://127.0.0.1:${server.address.port}/missing") },
                barcodeFallback = { code ->
                    fallbackCode = code
                    FoodProduct(code = code, name = "Bread", kcalPer100g = 240)
                }
            )
            val food = client.lookupBarcode("5999876432198").getOrThrow()
            assertEquals("5999876432198", fallbackCode)
            assertEquals("Bread", food.name)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun missingProductKeepsTheNotFoundMessage() = runBlocking {
        val client = FoodFactsClient(
            readJson = { error(FoodFactsClient.NotFound) },
            barcodeFallback = { error(FoodFactsClient.NotFound) }
        )
        assertEquals(FoodFactsClient.NotFound, client.lookupBarcode("123").exceptionOrNull()?.message)
    }

    @Test
    fun cancellationDoesNotStartTheFallback() = runBlocking {
        var fallbackCalled = false
        val client = FoodFactsClient(
            readJson = { throw CancellationException("closed") },
            barcodeFallback = {
                fallbackCalled = true
                error("unexpected")
            }
        )
        val error = runCatching { client.lookupBarcode("123") }.exceptionOrNull()
        assertTrue(error is CancellationException)
        assertFalse(fallbackCalled)
    }
}
