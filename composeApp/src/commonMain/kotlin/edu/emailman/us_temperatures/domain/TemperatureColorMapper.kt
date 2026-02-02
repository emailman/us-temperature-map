package edu.emailman.us_temperatures.domain

import androidx.compose.ui.graphics.Color

object TemperatureColorMapper {
    // Temperature color stops (Fahrenheit)
    private val colorStops = listOf(
        -20.0 to Color(0xFF0000FF),  // Deep Blue (very cold)
        0.0 to Color(0xFF0066FF),    // Blue
        20.0 to Color(0xFF4169E1),   // Royal Blue
        32.0 to Color(0xFF00FF00),   // Green (freezing)
        50.0 to Color(0xFFFFFF00),   // Yellow
        70.0 to Color(0xFFFFA500),   // Orange
        85.0 to Color(0xFFFF4500),   // Orange-Red
        100.0 to Color(0xFFDC143C),  // Crimson
        120.0 to Color(0xFF8B0000)   // Dark Red (extreme heat)
    )

    fun getColor(temperature: Double): Color {
        if (temperature.isNaN()) return Color.Gray

        // Clamp to range
        val temp = temperature.coerceIn(-20.0, 120.0)

        // Find the two color stops we're between
        for (i in 0 until colorStops.size - 1) {
            val (lowTemp, lowColor) = colorStops[i]
            val (highTemp, highColor) = colorStops[i + 1]

            if (temp >= lowTemp && temp <= highTemp) {
                val fraction = ((temp - lowTemp) / (highTemp - lowTemp)).toFloat()
                return lerpColor(lowColor, highColor, fraction)
            }
        }

        // Fallback to last color
        return colorStops.last().second
    }

    private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
        return Color(
            red = start.red + (end.red - start.red) * fraction,
            green = start.green + (end.green - start.green) * fraction,
            blue = start.blue + (end.blue - start.blue) * fraction,
            alpha = 1f
        )
    }

    fun getGradientColors(): List<Pair<Float, Color>> {
        return colorStops.map { (temp, color) ->
            val fraction = ((temp + 20.0) / 140.0).toFloat().coerceIn(0f, 1f)
            fraction to color
        }
    }
}
