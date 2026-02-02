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

    // Draw latitude lines (horizontal)
    for ((lat, y) in transformer.getLatitudeLines()) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f,
            pathEffect = dashEffect
        )

        // Label on left side
        val label = "${lat.toInt()}°N"
        val textResult = textMeasurer.measure(label, textStyle)
        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(4f, y - textResult.size.height / 2)
        )
    }

    // Draw longitude lines (vertical)
    for ((lon, x) in transformer.getLongitudeLines()) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f,
            pathEffect = dashEffect
        )

        // Label at bottom
        val label = "${(-lon).toInt()}°W"
        val textResult = textMeasurer.measure(label, textStyle)
        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(x - textResult.size.width / 2, size.height - textResult.size.height - 4f)
        )
    }
}
