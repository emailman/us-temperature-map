package edu.emailman.us_temperatures.util

object Constants {
    // US bounding box (continental US)
    const val US_LAT_MIN = 25.0
    const val US_LAT_MAX = 49.0
    const val US_LON_MIN = -125.0
    const val US_LON_MAX = -66.0

    // Data grid spacing (for API calls and heat map cells)
    const val GRID_LAT_SPACING = 5.0   // degrees
    const val GRID_LON_SPACING = 5.0   // degrees

    // Display grid spacing (for overlay lines)
    const val DISPLAY_GRID_LAT = 5.0
    const val DISPLAY_GRID_LON = 5.0

    // Temperature range (Fahrenheit)
    const val TEMP_MIN = 0.0
    const val TEMP_MAX = 100.0
}
