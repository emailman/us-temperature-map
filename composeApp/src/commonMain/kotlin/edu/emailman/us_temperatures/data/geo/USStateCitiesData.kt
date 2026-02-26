package edu.emailman.us_temperatures.data.geo

import edu.emailman.us_temperatures.data.model.City
import kotlinx.serialization.json.Json
import us_temperatures.composeapp.generated.resources.Res

object USStateCitiesData {
    private var cache: Map<String, List<City>>? = null

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadStateCities(): Map<String, List<City>> {
        cache?.let { return it }
        val bytes = Res.readBytes("files/us-state-cities.json")
        val result = json.decodeFromString<Map<String, List<City>>>(bytes.decodeToString())
        cache = result
        return result
    }
}
