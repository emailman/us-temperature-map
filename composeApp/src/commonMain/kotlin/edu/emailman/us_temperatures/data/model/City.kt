package edu.emailman.us_temperatures.data.model

import kotlinx.serialization.Serializable

@Serializable
data class City(
    val name: String,
    val state: String,
    val latitude: Double,
    val longitude: Double
)
