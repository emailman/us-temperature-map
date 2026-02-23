package edu.emailman.us_temperatures.data.cache

import edu.emailman.us_temperatures.data.model.CachedTemperatureResponse
import kotlinx.serialization.json.Json
import java.io.File

private val json = Json { ignoreUnknownKeys = true }

actual suspend fun loadCachedTemperatures(): CachedTemperatureResponse? {
    return try {
        // Try both project root and parent (Gradle run CWD is composeApp/)
        val candidates = listOf(
            File("dist/temperatures.json"),
            File("../dist/temperatures.json")
        )
        val file = candidates.firstOrNull { it.exists() } ?: return null
        json.decodeFromString<CachedTemperatureResponse>(file.readText())
    } catch (_: Exception) {
        null
    }
}
