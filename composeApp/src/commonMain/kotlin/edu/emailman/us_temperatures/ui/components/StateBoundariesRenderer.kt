package edu.emailman.us_temperatures.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import edu.emailman.us_temperatures.domain.StateBoundary

/**
 * Fill states with a solid color
 */
fun DrawScope.drawStateFills(
    states: List<StateBoundary>,
    fillColor: Color = Color(0xFFB8D4E8) // Light blue
) {
    states.forEach { state ->
        state.paths.forEach { path ->
            drawPath(
                path = path,
                color = fillColor,
                style = Fill
            )
        }
    }
}

/**
 * Draw individual state borders with thin lines
 */
fun DrawScope.drawStateBoundaries(
    states: List<StateBoundary>,
    borderColor: Color = Color(0xFF4A90D9), // Blue
    borderWidth: Float = 1f
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
 * Draw national border by drawing all outer state boundaries with thick line.
 * This creates the effect of a prominent national outline.
 */
fun DrawScope.drawNationalBorder(
    states: List<StateBoundary>,
    borderColor: Color = Color(0xFF4A90D9), // Blue
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
