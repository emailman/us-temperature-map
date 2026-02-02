package edu.emailman.us_temperatures.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import edu.emailman.us_temperatures.domain.StateBoundary

/**
 * Draw individual state borders with thin gray lines
 */
fun DrawScope.drawStateBoundaries(
    states: List<StateBoundary>,
    borderColor: Color = Color(0xFF808080), // Gray
    borderWidth: Float = 0.5f
) {
    states.forEach { state ->
        state.paths.forEach { path ->
            drawPath(
                path = path,
                color = borderColor,
                style = Stroke(width = borderWidth)
            )
        }
    }
}

/**
 * Draw national border by drawing all outer state boundaries with thick dark line.
 * This creates the effect of a prominent national outline.
 */
fun DrawScope.drawNationalBorder(
    states: List<StateBoundary>,
    borderColor: Color = Color(0xFF2E4A2E), // Dark green
    borderWidth: Float = 2f
) {
    // Draw all state paths - overlapping borders will reinforce the national outline
    states.forEach { state ->
        state.paths.forEach { path ->
            drawPath(
                path = path,
                color = borderColor,
                style = Stroke(width = borderWidth)
            )
        }
    }
}
