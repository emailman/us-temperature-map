package edu.emailman.us_temperatures.domain

import androidx.compose.ui.graphics.Path

/**
 * Domain model representing a US state's geographic boundary
 * @param name Full state name (e.g., "California")
 * @param code State ID from GeoJSON (e.g., "06")
 * @param polygons List of polygons, where each polygon is a list of rings,
 *                 and each ring is a list of (lat, lon) coordinate pairs.
 *                 First ring is exterior, subsequent rings are holes.
 */
data class StateGeometry(
    val name: String,
    val code: String,
    val polygons: List<List<List<Pair<Double, Double>>>>
)

/**
 * Screen-coordinate representation of a state boundary for rendering
 */
data class StateBoundary(
    val name: String,
    val code: String,
    val paths: List<Path>
)
