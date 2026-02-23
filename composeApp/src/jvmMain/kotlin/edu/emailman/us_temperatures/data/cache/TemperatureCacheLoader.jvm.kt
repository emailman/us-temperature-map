package edu.emailman.us_temperatures.data.cache

import edu.emailman.us_temperatures.data.model.CachedTemperatureResponse
import kotlinx.serialization.json.Json
import java.io.File

private val json = Json { ignoreUnknownKeys = true }

actual suspend fun loadCachedTemperatures(): CachedTemperatureResponse? {
    return try {
        val file = File("dist/temperatures.json")
        if (file.exists()) {
            json.decodeFromString<CachedTemperatureResponse>(file.readText())
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
