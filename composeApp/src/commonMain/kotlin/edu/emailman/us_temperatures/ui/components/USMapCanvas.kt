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
import androidx.compose.ui.input.pointer.PointerEventType
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
    hoveredCity: TemperatureData?,
    onCitySelected: (TemperatureData?) -> Unit,
    onCityHovered: (TemperatureData?) -> Unit,
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
                .pointerInput(cityDisplayData) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Move) {
                                val position = event.changes.first().position
                                val hovered = cityDisplayData.find { it.containsPoint(position.x, position.y) }
                                onCityHovered(hovered?.temperatureData)
                            }
                        }
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

            // Tooltip overlay for selected or hovered city (selected takes priority)
            val tooltipCity = selectedCity ?: hoveredCity
            tooltipCity?.let { city ->
                val displayData = cityDisplayData.find {
                    it.temperatureData.latitude == city.latitude &&
                    it.temperatureData.longitude == city.longitude
                }
                displayData?.let { data ->
                    // Tooltip dimensions (approximate)
                    val tooltipWidth = 180f
                    val tooltipHeight = 180f
                    val margin = 8f

                    // Map bounds from transformer
                    val mapLeft = transformer.offsetX
                    val mapRight = transformer.offsetX + transformer.mapWidth
                    val mapTop = transformer.offsetY
                    val mapBottom = transformer.offsetY + transformer.mapHeight

                    // Check available space in each direction
                    val spaceRight = mapRight - (data.screenX + data.radius)
                    val spaceLeft = data.screenX - data.radius - mapLeft
                    val spaceBelow = mapBottom - data.screenY
                    val spaceAbove = data.screenY - mapTop

                    // Determine horizontal position
                    val tooltipX = when {
                        spaceRight >= tooltipWidth + margin -> {
                            // Place to the right of marker
                            (data.screenX + data.radius + margin).toInt()
                        }
                        spaceLeft >= tooltipWidth + margin -> {
                            // Place to the left of marker
                            (data.screenX - data.radius - margin - tooltipWidth).toInt()
                        }
                        else -> {
                            // Center horizontally on the marker, clamped to map bounds
                            val centered = data.screenX - tooltipWidth / 2
                            centered.coerceIn(mapLeft, mapRight - tooltipWidth).toInt()
                        }
                    }

                    // Determine vertical position
                    val tooltipY = when {
                        // If we're placing tooltip to the side, try to center vertically on marker
                        spaceRight >= tooltipWidth + margin || spaceLeft >= tooltipWidth + margin -> {
                            val centered = data.screenY - tooltipHeight / 2
                            centered.coerceIn(mapTop, mapBottom - tooltipHeight).toInt()
                        }
                        // Otherwise place above or below
                        spaceAbove >= tooltipHeight + margin -> {
                            (data.screenY - data.radius - margin - tooltipHeight).toInt()
                        }
                        else -> {
                            (data.screenY + data.radius + margin).toInt()
                        }
                    }

                    CityTooltip(
                        city = city,
                        modifier = Modifier
                            .offset { IntOffset(tooltipX, tooltipY) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
