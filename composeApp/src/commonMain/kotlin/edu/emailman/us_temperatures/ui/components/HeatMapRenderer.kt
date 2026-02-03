package edu.emailman.us_temperatures.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.max
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import edu.emailman.us_temperatures.data.model.TemperatureData
import edu.emailman.us_temperatures.domain.CoordinateTransformer
import edu.emailman.us_temperatures.domain.TemperatureColorMapper
import edu.emailman.us_temperatures.ui.model.CityDisplayData
import edu.emailman.us_temperatures.util.Constants
import kotlin.math.roundToInt

fun DrawScope.drawHeatMap(
    temperatures: Map<Pair<Double, Double>, Double>,
    transformer: CoordinateTransformer,
    textMeasurer: TextMeasurer
) {
    for ((coords, temp) in temperatures) {
        val (lat, lon) = coords
        drawHeatMapCell(lat, lon, temp, transformer, textMeasurer)
    }
}

fun DrawScope.drawHeatMapCell(
    lat: Double,
    lon: Double,
    temperature: Double,
    transformer: CoordinateTransformer,
    textMeasurer: TextMeasurer
) {
    // Cell boundaries: center point ± half the grid spacing
    val halfLatSpacing = Constants.GRID_LAT_SPACING / 2
    val halfLonSpacing = Constants.GRID_LON_SPACING / 2

    val topLeft = transformer.toScreenCoords(
        lat + halfLatSpacing,
        lon - halfLonSpacing
    )
    val bottomRight = transformer.toScreenCoords(
        lat - halfLatSpacing,
        lon + halfLonSpacing
    )

    val color = TemperatureColorMapper.getColor(temperature)

    // Draw temperature text in center of cell (no background fill)
    if (!temperature.isNaN()) {
        val tempText = "${temperature.roundToInt()}°"
        val textStyle = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        val textResult = textMeasurer.measure(tempText, textStyle)

        // Calculate center of cell
        val centerX = (topLeft.x + bottomRight.x) / 2
        val centerY = (topLeft.y + bottomRight.y) / 2

        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(
                centerX - textResult.size.width / 2,
                centerY - textResult.size.height / 2
            )
        )

        // Draw circle around the temperature text
        val radius = max(textResult.size.width, textResult.size.height) / 2f + 8f
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 3f)
        )
    }
}

fun DrawScope.drawTemperatureDataList(
    temperatureList: List<TemperatureData>,
    transformer: CoordinateTransformer,
    textMeasurer: TextMeasurer
) {
    for (data in temperatureList) {
        if (!data.temperature.isNaN()) {
            drawHeatMapCell(
                data.latitude,
                data.longitude,
                data.temperature,
                transformer,
                textMeasurer
            )
        }
    }
}

fun DrawScope.drawCityMarkers(
    cities: List<TemperatureData>,
    transformer: CoordinateTransformer,
    textMeasurer: TextMeasurer,
    selectedCity: TemperatureData? = null
): List<CityDisplayData> {
    val displayDataList = mutableListOf<CityDisplayData>()

    for (city in cities) {
        if (city.temperature.isNaN()) continue

        val screenCoords = transformer.toScreenCoords(city.latitude, city.longitude)
        val color = TemperatureColorMapper.getColor(city.temperature)
        val isSelected = selectedCity?.let {
            it.latitude == city.latitude && it.longitude == city.longitude
        } ?: false

        val tempText = "${city.temperature.roundToInt()}°"
        val textStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        val textResult = textMeasurer.measure(tempText, textStyle)

        val radius = (max(textResult.size.width, textResult.size.height) / 2f + 10f) * 0.9f
        val center = Offset(screenCoords.x, screenCoords.y)

        // Draw filled circle background
        drawCircle(
            color = color.copy(alpha = 0.85f),
            radius = radius,
            center = center,
            style = Fill
        )

        // Draw border (thicker if selected)
        drawCircle(
            color = if (isSelected) Color.White else color,
            radius = radius,
            center = center,
            style = Stroke(width = if (isSelected) 4f else 2f)
        )

        // Draw temperature text
        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(
                center.x - textResult.size.width / 2,
                center.y - textResult.size.height / 2
            )
        )

        displayDataList.add(
            CityDisplayData(
                temperatureData = city,
                screenX = center.x,
                screenY = center.y,
                radius = radius
            )
        )
    }

    return displayDataList
}
