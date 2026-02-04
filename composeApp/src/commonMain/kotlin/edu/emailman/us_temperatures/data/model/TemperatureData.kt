package edu.emailman.us_temperatures.data.model

data class TemperatureData(
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
)
