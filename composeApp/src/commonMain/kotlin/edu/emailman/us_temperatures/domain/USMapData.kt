package edu.emailman.us_temperatures.domain

import androidx.compose.ui.graphics.Path

object USMapData {
    // Simplified US continental outline coordinates (lat, lon)
    // This is a rough approximation of the US border
    private val outlinePoints = listOf(
        // Pacific Northwest
        48.5 to -124.5,
        48.0 to -123.0,
        48.5 to -117.0,
        49.0 to -117.0,

        // Northern Border (following roughly along 49th parallel and Great Lakes)
        49.0 to -110.0,
        49.0 to -104.0,
        49.0 to -97.0,
        49.0 to -95.0,
        48.0 to -89.0,
        47.0 to -88.0,
        46.5 to -85.0,
        45.5 to -84.0,
        43.5 to -82.5,
        42.0 to -83.0,
        42.5 to -79.0,
        43.0 to -79.0,
        44.0 to -76.5,
        45.0 to -75.0,
        45.0 to -71.0,
        47.0 to -69.0,

        // Atlantic Coast
        45.0 to -67.0,
        44.0 to -68.5,
        43.0 to -70.5,
        42.0 to -70.0,
        41.0 to -71.5,
        41.0 to -73.0,
        40.5 to -74.0,
        39.0 to -75.0,
        38.0 to -75.5,
        37.0 to -76.0,
        36.0 to -76.0,
        35.0 to -76.0,
        34.0 to -78.0,
        33.0 to -79.5,
        32.0 to -81.0,
        31.0 to -81.5,
        30.0 to -81.5,

        // Florida
        29.0 to -81.0,
        27.5 to -80.5,
        25.5 to -80.5,
        25.0 to -81.0,
        25.5 to -82.0,
        27.0 to -83.0,
        28.5 to -83.0,
        30.0 to -84.0,

        // Gulf Coast
        30.0 to -86.0,
        30.5 to -88.0,
        30.0 to -89.5,
        29.5 to -90.0,
        29.0 to -91.0,
        29.5 to -93.0,
        29.5 to -95.0,
        28.5 to -96.5,
        27.0 to -97.5,
        26.0 to -97.5,

        // Texas-Mexico Border
        26.0 to -99.0,
        27.0 to -100.0,
        29.0 to -101.0,
        29.5 to -103.0,
        31.0 to -104.0,
        31.5 to -106.5,
        32.0 to -107.0,
        31.5 to -109.0,
        31.5 to -111.0,
        32.5 to -114.5,
        33.0 to -117.0,

        // California Coast
        34.5 to -120.5,
        35.5 to -121.0,
        37.0 to -122.5,
        38.5 to -123.0,
        40.0 to -124.0,
        42.0 to -124.5,
        46.0 to -124.0,
        48.5 to -124.5  // Close the loop
    )

    fun createPath(transformer: CoordinateTransformer): Path {
        val path = Path()

        if (outlinePoints.isEmpty()) return path

        val firstPoint = outlinePoints.first()
        val startOffset = transformer.toScreenCoords(firstPoint.first, firstPoint.second)
        path.moveTo(startOffset.x, startOffset.y)

        for (i in 1 until outlinePoints.size) {
            val (lat, lon) = outlinePoints[i]
            val offset = transformer.toScreenCoords(lat, lon)
            path.lineTo(offset.x, offset.y)
        }

        path.close()
        return path
    }
}
