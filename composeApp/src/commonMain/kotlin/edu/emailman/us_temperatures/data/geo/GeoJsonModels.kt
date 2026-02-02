package edu.emailman.us_temperatures.data.geo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GeoJsonFeatureCollection(
    val type: String,
    val features: List<GeoJsonFeature>
)

@Serializable
data class GeoJsonFeature(
    val type: String,
    val id: String? = null,
    val properties: StateProperties,
    val geometry: GeoJsonGeometry
)

@Serializable
data class StateProperties(
    val name: String,
    val density: Double? = null
)

@Serializable
data class GeoJsonGeometry(
    val type: String,
    val coordinates: JsonElement
)
