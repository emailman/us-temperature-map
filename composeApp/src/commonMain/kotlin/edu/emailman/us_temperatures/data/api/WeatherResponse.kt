package edu.emailman.us_temperatures.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val coord: Coord,
    val main: Main,
    val name: String,
    val weather: List<Weather> = emptyList(),
    val wind: Wind? = null
)

@Serializable
data class Wind(
    val speed: Double,
    val deg: Int = 0
)

@Serializable
data class Weather(
    val main: String,
    val description: String
)

@Serializable
data class Coord(
    val lon: Double,
    val lat: Double
)

@Serializable
data class Main(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    val pressure: Int,
    val humidity: Int
)
