package edu.emailman.us_temperatures.domain

import androidx.compose.ui.geometry.Offset
import edu.emailman.us_temperatures.util.Constants
import kotlin.math.PI
import kotlin.math.cos

class CoordinateTransformer(
    private val canvasWidth: Float,
    private val canvasHeight: Float,
    private val latMin: Double = Constants.US_LAT_MIN,
    private val latMax: Double = Constants.US_LAT_MAX,
    private val lonMin: Double = Constants.US_LON_MIN,
    private val lonMax: Double = Constants.US_LON_MAX
) {
    private val latRange = latMax - latMin
    private val lonRange = lonMax - lonMin
    private val centerLat = (latMin + latMax) / 2

    // Map dimensions that preserve aspect ratio
    val mapWidth: Float
    val mapHeight: Float
    val offsetX: Float
    val offsetY: Float

    init {
        val availableAspectRatio = canvasWidth / canvasHeight
        val targetAspectRatio = (lonRange / latRange * cos(centerLat * PI / 180.0)).toFloat()

        if (availableAspectRatio > targetAspectRatio) {
            // Available space is wider than target ratio - letterbox horizontally
            mapHeight = canvasHeight
            mapWidth = canvasHeight * targetAspectRatio
            offsetX = (canvasWidth - mapWidth) / 2
            offsetY = 0f
        } else {
            // Available space is taller than target ratio - letterbox vertically
            mapWidth = canvasWidth
            mapHeight = canvasWidth / targetAspectRatio
            offsetX = 0f
            offsetY = (canvasHeight - mapHeight) / 2
        }
    }

    fun toScreenCoords(lat: Double, lon: Double): Offset {
        // Canvas origin is top-left, Y increases downward
        // Higher latitude = north = lower Y value
        val x = ((lon - lonMin) / lonRange * mapWidth).toFloat() + offsetX
        val y = ((latMax - lat) / latRange * mapHeight).toFloat() + offsetY
        return Offset(x, y)
    }

    fun toLatLon(screenX: Float, screenY: Float): Pair<Double, Double> {
        val adjustedX = screenX - offsetX
        val adjustedY = screenY - offsetY
        val lon = (adjustedX / mapWidth) * lonRange + lonMin
        val lat = latMax - (adjustedY / mapHeight) * latRange
        return Pair(lat, lon)
    }

    fun getLatitudeLines(): List<Pair<Double, Float>> {
        val lines = mutableListOf<Pair<Double, Float>>()
        var lat = Constants.US_LAT_MIN
        while (lat <= Constants.US_LAT_MAX) {
            val y = ((latMax - lat) / latRange * mapHeight).toFloat() + offsetY
            lines.add(Pair(lat, y))
            lat += Constants.DISPLAY_GRID_LAT
        }
        return lines
    }

    fun getLongitudeLines(): List<Pair<Double, Float>> {
        val lines = mutableListOf<Pair<Double, Float>>()
        var lon = Constants.US_LON_MIN
        while (lon <= Constants.US_LON_MAX) {
            val x = ((lon - lonMin) / lonRange * mapWidth).toFloat() + offsetX
            lines.add(Pair(lon, x))
            lon += Constants.DISPLAY_GRID_LON
        }
        return lines
    }

    fun isWithinMapBounds(screenX: Float, screenY: Float): Boolean {
        return screenX >= offsetX && screenX <= offsetX + mapWidth &&
               screenY >= offsetY && screenY <= offsetY + mapHeight
    }
}
