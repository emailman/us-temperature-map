package edu.emailman.us_temperatures.data.model

data class TemperatureData(
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val locationName: String = ""
)
