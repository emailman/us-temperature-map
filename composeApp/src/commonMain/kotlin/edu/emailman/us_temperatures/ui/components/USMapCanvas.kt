package edu.emailman.us_temperatures.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import edu.emailman.us_temperatures.data.geo.USStatesGeoData
import edu.emailman.us_temperatures.data.model.TemperatureData
import edu.emailman.us_temperatures.domain.CoordinateTransformer
import edu.emailman.us_temperatures.domain.StateBoundary
import edu.emailman.us_temperatures.domain.StateGeometry
import edu.emailman.us_temperatures.domain.StatePathConverter
import edu.emailman.us_temperatures.ui.model.CityDisplayData

@Composable
fun USMapCanvas(
    cityTemperatures: List<TemperatureData>,
    selectedCity: TemperatureData?,
    onCitySelected: (TemperatureData?) -> Unit,
    showGrid: Boolean = true,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var stateGeometries by remember { mutableStateOf<List<StateGeometry>>(emptyList()) }
    var cityDisplayData by remember { mutableStateOf<List<CityDisplayData>>(emptyList()) }

    // Load state geometries once
    LaunchedEffect(Unit) {
        stateGeometries = USStatesGeoData.loadStates()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8F4FC)) // Very light blue
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(cityDisplayData) {
                    detectTapGestures { offset ->
                        val tappedCity = cityDisplayData.find { it.containsPoint(offset.x, offset.y) }
                        onCitySelected(tappedCity?.temperatureData)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Layer 1: State fills (light blue background)
                drawStateFills(
                    states = stateBoundaries,
                    fillColor = Color(0xFFB8D4E8)
                )

                // Layer 2: State borders (blue)
                drawStateBoundaries(
                    states = stateBoundaries,
                    borderColor = Color(0xFF4A90D9),
                    borderWidth = 1f
                )

                // Layer 3: National border (thicker blue)
                drawNationalBorder(
                    states = stateBoundaries,
                    borderColor = Color(0xFF4A90D9),
                    borderWidth = 2f
                )

                // Layer 4: Grid overlay
                drawGridOverlay(transformer, textMeasurer, showGrid)

                // Layer 5: City markers (foreground, on top of everything)
                cityDisplayData = drawCityMarkers(
                    cities = cityTemperatures,
                    transformer = transformer,
                    textMeasurer = textMeasurer,
                    selectedCity = selectedCity
                )
            }

            // Tooltip overlay for selected city
            selectedCity?.let { city ->
                val displayData = cityDisplayData.find {
                    it.temperatureData.latitude == city.latitude &&
                    it.temperatureData.longitude == city.longitude
                }
                displayData?.let { data ->
                    val isLowLatitude = city.latitude < 30.0
                    val isEastCoast = city.longitude > -75.0
                    val tooltipOffsetX = when {
                        isLowLatitude -> with(density) { (data.screenX - 60).toInt() }  // Center above marker
                        isEastCoast -> with(density) { (data.screenX - 160).toInt() }  // Left of marker
                        else -> with(density) { (data.screenX + data.radius + 8).toInt() }  // Right of marker
                    }
                    val tooltipOffsetY = if (isLowLatitude) {
                        with(density) { (data.screenY - 180).toInt() }  // Above marker
                    } else {
                        with(density) { (data.screenY - 40).toInt() }  // Current position
                    }

                    CityTooltip(
                        city = city,
                        modifier = Modifier
                            .offset { IntOffset(tooltipOffsetX, tooltipOffsetY) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
