package edu.emailman.us_temperatures.data.geo

import edu.emailman.us_temperatures.domain.StateGeometry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object GeoJsonParser {
    private val json = Json { ignoreUnknownKeys = true }

    // States to exclude (non-continental US)
    private val excludedStates = setOf(
        "Alaska",
        "Hawaii",
        "Puerto Rico",
        "Guam",
        "American Samoa",
        "United States Virgin Islands",
        "Northern Mariana Islands"
    )

    fun parse(geoJsonString: String): List<StateGeometry> {
        val featureCollection = json.decodeFromString<GeoJsonFeatureCollection>(geoJsonString)

        return featureCollection.features
            .filter { it.properties.name !in excludedStates }
            .mapNotNull { feature ->
                try {
                    val polygons = parseGeometry(feature.geometry)
                    if (polygons.isNotEmpty()) {
                        StateGeometry(
                            name = feature.properties.name,
                            code = feature.id ?: "",
                            polygons = polygons
                        )
                    } else null
                } catch (e: Exception) {
                    null // Skip malformed features
                }
            }
    }

    private fun parseGeometry(geometry: GeoJsonGeometry): List<List<List<Pair<Double, Double>>>> {
        return when (geometry.type) {
            "Polygon" -> listOf(parsePolygon(geometry.coordinates.jsonArray))
            "MultiPolygon" -> parseMultiPolygon(geometry.coordinates.jsonArray)
            else -> emptyList()
        }
    }

    // Polygon: [ [ring], [ring], ... ] where ring is array of [lon, lat] points
    private fun parsePolygon(coordinates: JsonArray): List<List<Pair<Double, Double>>> {
        return coordinates.map { ring ->
            ring.jsonArray.map { point ->
                val coords = point.jsonArray
                val lon = coords[0].jsonPrimitive.double
                val lat = coords[1].jsonPrimitive.double
                // Convert from GeoJSON [lon, lat] to domain (lat, lon)
                Pair(lat, lon)
            }
        }
    }

    // MultiPolygon: [ polygon, polygon, ... ]
    private fun parseMultiPolygon(coordinates: JsonArray): List<List<List<Pair<Double, Double>>>> {
        return coordinates.flatMap { polygon ->
            listOf(parsePolygon(polygon.jsonArray))
        }
    }
}
