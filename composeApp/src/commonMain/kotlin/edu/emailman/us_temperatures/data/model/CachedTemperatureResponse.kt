package edu.emailman.us_temperatures.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CachedTemperatureResponse(
    val fetchedAt: String,
    val cityCount: Int,
    val temperatures: List<CachedTemperatureEntry>
)

@Serializable
data class CachedTemperatureEntry(
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val locationName: String = "",
    val cityName: String = "",
    val stateName: String = "",
    val weatherCondition: String = "",
    val weatherDescription: String = "",
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val windDirection: Int = 0,
    val tempMin: Double = 0.0,
    val tempMax: Double = 0.0
) {
    fun toTemperatureData(): TemperatureData = TemperatureData(
        latitude = latitude,
        longitude = longitude,
        temperature = temperature,
        locationName = locationName,
        cityName = cityName,
        stateName = stateName,
        weatherCondition = weatherCondition,
        weatherDescription = weatherDescription,
        humidity = humidity,
        windSpeed = windSpeed,
        windDirection = windDirection,
        tempMin = tempMin,
        tempMax = tempMax
    )
}
