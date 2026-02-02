package edu.emailman.us_temperatures.data.repository

import edu.emailman.us_temperatures.data.api.OpenWeatherMapApi
import edu.emailman.us_temperatures.data.model.TemperatureData
import edu.emailman.us_temperatures.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepository(private val api: OpenWeatherMapApi) {

    companion object {
        fun generateGridPoints(): List<Pair<Double, Double>> {
            val points = mutableListOf<Pair<Double, Double>>()
            // Start at cell centers (midway between grid lines) so cells align with display grid
            val latStart = Constants.US_LAT_MIN + Constants.GRID_LAT_SPACING / 2
            val lonStart = Constants.US_LON_MIN + Constants.GRID_LON_SPACING / 2
            var lat = latStart
            while (lat < Constants.US_LAT_MAX) {
                var lon = lonStart
                while (lon < Constants.US_LON_MAX) {
                    points.add(Pair(lat, lon))
                    lon += Constants.GRID_LON_SPACING
                }
                lat += Constants.GRID_LAT_SPACING
            }
            return points
        }
    }

    fun fetchTemperaturesProgressively(): Flow<TemperatureData> = flow {
        val gridPoints = generateGridPoints()

        for ((lat, lon) in gridPoints) {
            try {
                val response = api.getCurrentWeather(lat, lon)
                emit(
                    TemperatureData(
                        latitude = lat,
                        longitude = lon,
                        temperature = response.main.temp,
                        locationName = response.name
                    )
                )
                // Rate limiting: OpenWeatherMap free tier allows 60 calls/minute
                delay(1100) // ~55 calls per minute to stay safe
            } catch (e: Exception) {
                // Emit with default temperature on error
                emit(
                    TemperatureData(
                        latitude = lat,
                        longitude = lon,
                        temperature = Double.NaN,
                        locationName = "Error"
                    )
                )
            }
        }
    }

    suspend fun fetchAllTemperatures(): List<TemperatureData> {
        val results = mutableListOf<TemperatureData>()
        fetchTemperaturesProgressively().collect { results.add(it) }
        return results
    }
}
