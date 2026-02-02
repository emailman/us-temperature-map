package edu.emailman.us_temperatures.domain

import androidx.compose.ui.geometry.Offset
import edu.emailman.us_temperatures.util.Constants

class CoordinateTransformer(
    private val canvasWidth: Float,
    private val canvasHeight: Float
) {
    private val latRange = Constants.US_LAT_MAX - Constants.US_LAT_MIN
    private val lonRange = Constants.US_LON_MAX - Constants.US_LON_MIN

    fun toScreenCoords(lat: Double, lon: Double): Offset {
        // Canvas origin is top-left, Y increases downward
        // Higher latitude = north = lower Y value
        val x = ((lon - Constants.US_LON_MIN) / lonRange * canvasWidth).toFloat()
        val y = ((Constants.US_LAT_MAX - lat) / latRange * canvasHeight).toFloat()
        return Offset(x, y)
    }

    fun toLatLon(screenX: Float, screenY: Float): Pair<Double, Double> {
        val lon = (screenX / canvasWidth) * lonRange + Constants.US_LON_MIN
        val lat = Constants.US_LAT_MAX - (screenY / canvasHeight) * latRange
        return Pair(lat, lon)
    }

    fun getLatitudeLines(): List<Pair<Double, Float>> {
        val lines = mutableListOf<Pair<Double, Float>>()
        var lat = Constants.US_LAT_MIN
        while (lat <= Constants.US_LAT_MAX) {
            val y = ((Constants.US_LAT_MAX - lat) / latRange * canvasHeight).toFloat()
            lines.add(Pair(lat, y))
            lat += Constants.DISPLAY_GRID_LAT
        }
        return lines
    }

    fun getLongitudeLines(): List<Pair<Double, Float>> {
        val lines = mutableListOf<Pair<Double, Float>>()
        var lon = Constants.US_LON_MIN
        while (lon <= Constants.US_LON_MAX) {
            val x = ((lon - Constants.US_LON_MIN) / lonRange * canvasWidth).toFloat()
            lines.add(Pair(lon, x))
            lon += Constants.DISPLAY_GRID_LON
        }
        return lines
    }
}
