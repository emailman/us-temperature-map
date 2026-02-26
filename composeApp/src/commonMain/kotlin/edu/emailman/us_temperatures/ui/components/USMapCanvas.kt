package edu.emailman.us_temperatures.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import edu.emailman.us_temperatures.domain.PointInPolygon
import edu.emailman.us_temperatures.domain.StateBoundary
import edu.emailman.us_temperatures.domain.StateGeometry
import edu.emailman.us_temperatures.domain.StatePathConverter
import edu.emailman.us_temperatures.ui.model.CityDisplayData
import edu.emailman.us_temperatures.viewmodel.StateDetailState
import edu.emailman.us_temperatures.viewmodel.ViewMode

@Composable
fun USMapCanvas(
    cityTemperatures: List<TemperatureData>,
    selectedCity: TemperatureData?,
    hoveredCity: TemperatureData?,
    onCitySelected: (TemperatureData?) -> Unit,
    onCityHovered: (TemperatureData?) -> Unit,
    showGrid: Boolean = true,
    viewMode: ViewMode = ViewMode.TEMPERATURE_MAP,
    selectedStateName: String? = null,
    stateDetailState: StateDetailState = StateDetailState.Idle,
    onStateSelected: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var stateGeometries by remember { mutableStateOf<List<StateGeometry>>(emptyList()) }
    var cityDisplayData by remember { mutableStateOf<List<CityDisplayData>>(emptyList()) }

    LaunchedEffect(Unit) {
        stateGeometries = USStatesGeoData.loadStates()
    }

    // Reset hit-test data when switching view context
    LaunchedEffect(viewMode, selectedStateName) {
        cityDisplayData = emptyList()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8F4FC))
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        // US-level transformer used for Branch A and B
        val transformer = remember(widthPx, heightPx) {
            CoordinateTransformer(widthPx, heightPx)
        }

        // All state boundaries at US scale
        val stateBoundaries: List<StateBoundary> = remember(stateGeometries, widthPx, heightPx) {
            if (stateGeometries.isNotEmpty()) {
                StatePathConverter.convertAll(stateGeometries, transformer)
            } else {
                emptyList()
            }
        }

        // State detail data: zoomed transformer + single-state boundaries (Branch C)
        val stateDetailData: Pair<CoordinateTransformer, List<StateBoundary>>? =
            remember(selectedStateName, widthPx, heightPx, stateGeometries) {
                if (selectedStateName != null && stateGeometries.isNotEmpty()) {
                    val selectedState = stateGeometries.find { it.name == selectedStateName }
                    if (selectedState != null) {
                        val allPoints = selectedState.polygons.flatten().flatten()
                        if (allPoints.isNotEmpty()) {
                            val minLat = allPoints.minOf { it.first }
                            val maxLat = allPoints.maxOf { it.first }
                            val minLon = allPoints.minOf { it.second }
                            val maxLon = allPoints.maxOf { it.second }
                            val latPad = (maxLat - minLat) * 0.10
                            val lonPad = (maxLon - minLon) * 0.10
                            val t = CoordinateTransformer(
                                widthPx, heightPx,
                                minLat - latPad, maxLat + latPad,
                                minLon - lonPad, maxLon + lonPad
                            )
                            val b = listOf(StatePathConverter.convert(selectedState, t))
                            Pair(t, b)
                        } else null
                    } else null
                } else null
            }

        when {
            // Branch C: STATE_SELECT with a specific state chosen (zoomed detail)
            viewMode == ViewMode.STATE_SELECT && selectedStateName != null -> {
                val detailTransformer = stateDetailData?.first
                val detailBoundaries = stateDetailData?.second ?: emptyList()

                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(cityDisplayData) {
                                detectTapGestures { offset ->
                                    val tapped = cityDisplayData.find {
                                        it.containsPoint(offset.x, offset.y)
                                    }
                                    onCitySelected(tapped?.temperatureData)
                                }
                            }
                            .pointerInput(cityDisplayData) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.type == PointerEventType.Move) {
                                            val position = event.changes.first().position
                                            val hovered = cityDisplayData.find {
                                                it.containsPoint(position.x, position.y)
                                            }
                                            onCityHovered(hovered?.temperatureData)
                                        }
                                    }
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawStateFills(detailBoundaries, Color(0xFFB8D4E8))
                            drawStateBoundaries(detailBoundaries, Color(0xFF4A90D9), 1f)
                            drawNationalBorder(detailBoundaries, Color(0xFF4A90D9), 2f)

                            if (detailTransformer != null &&
                                stateDetailState is StateDetailState.Success
                            ) {
                                cityDisplayData = drawCityMarkers(
                                    cities = stateDetailState.temperatures,
                                    transformer = detailTransformer,
                                    textMeasurer = textMeasurer,
                                    selectedCity = selectedCity
                                )
                            }
                        }

                        // State detail status overlays
                        when (stateDetailState) {
                            is StateDetailState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            is StateDetailState.NoApiKey -> {
                                Text(
                                    text = "State detail requires a live API connection",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            is StateDetailState.Error -> {
                                Text(
                                    text = "Error: ${stateDetailState.message}",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            is StateDetailState.Success -> {
                                // City tooltip (selected takes priority over hovered)
                                val tooltipCity = selectedCity ?: hoveredCity
                                CityTooltipOverlay(
                                    tooltipCity = tooltipCity,
                                    cityDisplayData = cityDisplayData,
                                    transformer = detailTransformer ?: transformer
                                )
                            }
                            else -> {}
                        }
                    }

                    // Back button — always visible in detail view
                    TextButton(
                        onClick = { onStateSelected(null) },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    ) {
                        Text("\u2190 All States")
                    }
                }
            }

            // Branch B: STATE_SELECT with no state chosen (clickable overview)
            viewMode == ViewMode.STATE_SELECT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(stateGeometries) {
                            detectTapGestures { offset ->
                                val (lat, lon) = transformer.toLatLon(offset.x, offset.y)
                                val state = stateGeometries.find {
                                    PointInPolygon.isPointInState(lat, lon, it)
                                }
                                onStateSelected(state?.name)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawStateFills(stateBoundaries, Color(0xFFB8D4E8))
                        drawStateBoundaries(stateBoundaries, Color(0xFF4A90D9), 1f)
                        drawNationalBorder(stateBoundaries, Color(0xFF4A90D9), 2f)
                    }
                }
            }

            // Branch A: Full US temperature map (original behavior)
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(cityDisplayData) {
                            detectTapGestures { offset ->
                                val tappedCity = cityDisplayData.find {
                                    it.containsPoint(offset.x, offset.y)
                                }
                                onCitySelected(tappedCity?.temperatureData)
                            }
                        }
                        .pointerInput(cityDisplayData) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.type == PointerEventType.Move) {
                                        val position = event.changes.first().position
                                        val hovered = cityDisplayData.find {
                                            it.containsPoint(position.x, position.y)
                                        }
                                        onCityHovered(hovered?.temperatureData)
                                    }
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawStateFills(stateBoundaries, Color(0xFFB8D4E8))
                        drawStateBoundaries(stateBoundaries, Color(0xFF4A90D9), 1f)
                        drawNationalBorder(stateBoundaries, Color(0xFF4A90D9), 2f)
                        drawGridOverlay(transformer, textMeasurer, showGrid)
                        cityDisplayData = drawCityMarkers(
                            cities = cityTemperatures,
                            transformer = transformer,
                            textMeasurer = textMeasurer,
                            selectedCity = selectedCity
                        )
                    }

                    // City tooltip (selected takes priority over hovered)
                    val tooltipCity = selectedCity ?: hoveredCity
                    CityTooltipOverlay(
                        tooltipCity = tooltipCity,
                        cityDisplayData = cityDisplayData,
                        transformer = transformer
                    )
                }
            }
        }
    }
}

@Composable
private fun CityTooltipOverlay(
    tooltipCity: TemperatureData?,
    cityDisplayData: List<CityDisplayData>,
    transformer: CoordinateTransformer
) {
    tooltipCity?.let { city ->
        val displayData = cityDisplayData.find {
            it.temperatureData.latitude == city.latitude &&
            it.temperatureData.longitude == city.longitude
        }
        displayData?.let { data ->
            val tooltipWidth = 180f
            val tooltipHeight = 180f
            val margin = 8f

            val mapLeft = transformer.offsetX
            val mapRight = transformer.offsetX + transformer.mapWidth
            val mapTop = transformer.offsetY
            val mapBottom = transformer.offsetY + transformer.mapHeight

            val spaceRight = mapRight - (data.screenX + data.radius)
            val spaceLeft = data.screenX - data.radius - mapLeft
            val spaceAbove = data.screenY - mapTop

            val tooltipX = when {
                spaceRight >= tooltipWidth + margin -> {
                    (data.screenX + data.radius + margin).toInt()
                }
                spaceLeft >= tooltipWidth + margin -> {
                    (data.screenX - data.radius - margin - tooltipWidth).toInt()
                }
                else -> {
                    val centered = data.screenX - tooltipWidth / 2
                    centered.coerceIn(mapLeft, mapRight - tooltipWidth).toInt()
                }
            }

            val tooltipY = when {
                spaceRight >= tooltipWidth + margin || spaceLeft >= tooltipWidth + margin -> {
                    val centered = data.screenY - tooltipHeight / 2
                    centered.coerceIn(mapTop, mapBottom - tooltipHeight).toInt()
                }
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
