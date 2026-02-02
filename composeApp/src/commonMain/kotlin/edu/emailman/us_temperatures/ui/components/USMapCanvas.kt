package edu.emailman.us_temperatures.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import edu.emailman.us_temperatures.data.geo.USStatesGeoData
import edu.emailman.us_temperatures.domain.CoordinateTransformer
import edu.emailman.us_temperatures.domain.StateBoundary
import edu.emailman.us_temperatures.domain.StateGeometry
import edu.emailman.us_temperatures.domain.StatePathConverter

@Composable
fun USMapCanvas(
    temperatures: Map<Pair<Double, Double>, Double>,
    showGrid: Boolean = true,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var stateGeometries by remember { mutableStateOf<List<StateGeometry>>(emptyList()) }

    // Load state geometries once
    LaunchedEffect(Unit) {
        stateGeometries = USStatesGeoData.loadStates()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFD3D3D3)) // Light gray background
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        // Create transformer and convert paths in composable context
        val transformer = remember(widthPx, heightPx) {
            CoordinateTransformer(widthPx, heightPx)
        }

        val stateBoundaries: List<StateBoundary> = remember(stateGeometries, widthPx, heightPx) {
            if (stateGeometries.isNotEmpty()) {
                StatePathConverter.convertAll(stateGeometries, transformer)
            } else {
                emptyList()
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Layer 1: Heat map cells (bottom)
            drawHeatMap(temperatures, transformer, textMeasurer)

            // Layer 2: State borders (thin gray, 0.5px)
            drawStateBoundaries(
                states = stateBoundaries,
                borderColor = Color(0xFF808080),
                borderWidth = 0.5f
            )

            // Layer 3: National border (thick dark green, 2px)
            drawNationalBorder(
                states = stateBoundaries,
                borderColor = Color(0xFF2E4A2E),
                borderWidth = 2f
            )

            // Layer 4: Grid overlay (top)
            drawGridOverlay(transformer, textMeasurer, showGrid)
        }
    }
}
