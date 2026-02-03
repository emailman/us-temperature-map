package edu.emailman.us_temperatures.data.geo

import edu.emailman.us_temperatures.data.model.City
import kotlinx.serialization.json.Json
import us_temperatures.composeapp.generated.resources.Res

object USCitiesData {
    private var cachedCities: List<City>? = null

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadCities(): List<City> {
        cachedCities?.let { return it }

        val bytes = Res.readBytes("files/us-cities.json")
        val jsonString = bytes.decodeToString()
        val cities = json.decodeFromString<List<City>>(jsonString)

        cachedCities = cities
        return cities
    }

    fun getCachedCities(): List<City>? = cachedCities
}
