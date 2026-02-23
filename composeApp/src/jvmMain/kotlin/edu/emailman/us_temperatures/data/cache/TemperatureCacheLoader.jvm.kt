package edu.emailman.us_temperatures.data.cache

import edu.emailman.us_temperatures.data.model.CachedTemperatureResponse
import kotlinx.serialization.json.Json
import java.io.File

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

private fun findCacheFile(): File? {
    // Try both project root and parent (Gradle run CWD is composeApp/)
    return listOf(
        File("dist/temperatures.json"),
        File("../dist/temperatures.json")
    ).firstOrNull { it.exists() }
}

actual suspend fun loadCachedTemperatures(): CachedTemperatureResponse? {
    return try {
        val file = findCacheFile() ?: return null
        json.decodeFromString<CachedTemperatureResponse>(file.readText())
    } catch (_: Exception) {
        null
    }
}

actual fun saveCachedTemperatures(response: CachedTemperatureResponse) {
    try {
        val file = findCacheFile() ?: File("dist/temperatures.json")
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(CachedTemperatureResponse.serializer(), response))
        println("Saved ${response.cityCount} temperatures to ${file.absolutePath}")
    } catch (e: Exception) {
        println("Failed to save temperatures cache: ${e.message}")
    }
}
