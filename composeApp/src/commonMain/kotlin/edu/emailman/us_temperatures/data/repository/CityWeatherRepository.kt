package edu.emailman.us_temperatures.data.repository

import edu.emailman.us_temperatures.data.api.OpenWeatherMapApi
import edu.emailman.us_temperatures.data.model.City
import edu.emailman.us_temperatures.data.model.TemperatureData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CityWeatherRepository(private val api: OpenWeatherMapApi) {

    fun fetchCityTemperaturesProgressively(cities: List<City>): Flow<TemperatureData> = flow {
        for (city in cities) {
            try {
                val response = api.getCurrentWeather(city.latitude, city.longitude)
                val weather = response.weather.firstOrNull()
                emit(
                    TemperatureData(
                        latitude = city.latitude,
                        longitude = city.longitude,
                        temperature = response.main.temp,
                        locationName = response.name,
                        cityName = city.name,
                        stateName = city.state,
                        weatherCondition = weather?.main ?: "",
                        weatherDescription = weather?.description ?: "",
                        humidity = response.main.humidity,
                        windSpeed = response.wind?.speed ?: 0.0,
                        windDirection = response.wind?.deg ?: 0,
                        tempMin = response.main.tempMin,
                        tempMax = response.main.tempMax
                    )
                )
                // Rate limiting: OpenWeatherMap free tier allows 60 calls/minute
                delay(1100) // ~55 calls per minute to stay safe
            } catch (e: Exception) {
                // Emit with default temperature on error
                emit(
                    TemperatureData(
                        latitude = city.latitude,
                        longitude = city.longitude,
                        temperature = Double.NaN,
                        locationName = "Error",
                        cityName = city.name,
                        stateName = city.state,
                        weatherCondition = "",
                        weatherDescription = ""
                    )
                )
            }
        }
    }

    suspend fun fetchAllCityTemperatures(cities: List<City>): List<TemperatureData> {
        val results = mutableListOf<TemperatureData>()
        fetchCityTemperaturesProgressively(cities).collect { results.add(it) }
        return results
    }

    suspend fun fetchCitiesParallel(cities: List<City>): List<TemperatureData> {
        return coroutineScope {
            cities.map { city ->
                async {
                    try {
                        val response = api.getCurrentWeather(city.latitude, city.longitude)
                        val weather = response.weather.firstOrNull()
                        TemperatureData(
                            latitude = city.latitude,
                            longitude = city.longitude,
                            temperature = response.main.temp,
                            locationName = response.name,
                            cityName = city.name,
                            stateName = city.state,
                            weatherCondition = weather?.main ?: "",
                            weatherDescription = weather?.description ?: "",
                            humidity = response.main.humidity,
                            windSpeed = response.wind?.speed ?: 0.0,
                            windDirection = response.wind?.deg ?: 0,
                            tempMin = response.main.tempMin,
                            tempMax = response.main.tempMax
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }
}
