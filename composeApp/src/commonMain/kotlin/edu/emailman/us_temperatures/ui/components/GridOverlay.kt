package edu.emailman.us_temperatures.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import edu.emailman.us_temperatures.domain.CoordinateTransformer

fun DrawScope.drawGridOverlay(
    transformer: CoordinateTransformer,
    textMeasurer: TextMeasurer,
    showGrid: Boolean = true
) {
    if (!showGrid) return

    val gridColor = Color.Gray.copy(alpha = 0.5f)
    val labelColor = Color.DarkGray
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val textStyle = TextStyle(fontSize = 10.sp, color = labelColor)

    // Draw latitude lines (horizontal) - within map bounds
    for ((lat, y) in transformer.getLatitudeLines()) {
        drawLine(
            color = gridColor,
            start = Offset(transformer.offsetX, y),
            end = Offset(transformer.offsetX + transformer.mapWidth, y),
            strokeWidth = 1f,
            pathEffect = dashEffect
        )

        // Label on left side (within map area)
        val label = "${lat.toInt()}°N"
        val textResult = textMeasurer.measure(label, textStyle)
        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(transformer.offsetX + 4f, y - textResult.size.height / 2)
        )
    }

    // Draw longitude lines (vertical) - within map bounds
    for ((lon, x) in transformer.getLongitudeLines()) {
        drawLine(
            color = gridColor,
            start = Offset(x, transformer.offsetY),
            end = Offset(x, transformer.offsetY + transformer.mapHeight),
            strokeWidth = 1f,
            pathEffect = dashEffect
        )

        // Label at bottom (within map area)
        val label = "${(-lon).toInt()}°W"
        val textResult = textMeasurer.measure(label, textStyle)
        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(x - textResult.size.width / 2, transformer.offsetY + transformer.mapHeight - textResult.size.height - 4f)
        )
    }
}
