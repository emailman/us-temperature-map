package edu.emailman.us_temperatures.ui.model

import edu.emailman.us_temperatures.data.model.TemperatureData

data class CityDisplayData(
    val temperatureData: TemperatureData,
    val screenX: Float,
    val screenY: Float,
    val radius: Float
) {
    fun containsPoint(x: Float, y: Float): Boolean {
        val dx = x - screenX
        val dy = y - screenY
        return (dx * dx + dy * dy) <= (radius * radius)
    }
}
