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

    // Geographic aspect ratio (width/height) for proper map proportions
    // Must account for longitude compression at mid-latitudes: cos(center_latitude)
    // Center latitude = (25 + 49) / 2 = 37°, cos(37°) ≈ 0.7986
    private const val CENTER_LAT_RADIANS = 0.6458  // 37 degrees in radians
    private const val LON_CORRECTION = 0.7986      // cos(37°)
    const val US_ASPECT_RATIO = ((US_LON_MAX - US_LON_MIN) * LON_CORRECTION) / (US_LAT_MAX - US_LAT_MIN)  // ~1.96
}
