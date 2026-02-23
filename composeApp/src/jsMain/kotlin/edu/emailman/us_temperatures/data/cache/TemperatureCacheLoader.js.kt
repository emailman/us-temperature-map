package edu.emailman.us_temperatures.data.cache

import edu.emailman.us_temperatures.data.model.CachedTemperatureResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

actual fun saveCachedTemperatures(response: CachedTemperatureResponse) {}

actual suspend fun loadCachedTemperatures(): CachedTemperatureResponse? {
    return try {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val cacheBuster = kotlin.random.Random.nextLong()
        val response: CachedTemperatureResponse = client.get("temperatures.json?t=$cacheBuster").body()
        client.close()
        response
    } catch (e: Exception) {
        null
    }
}
