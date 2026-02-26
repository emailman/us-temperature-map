package edu.emailman.us_temperatures.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.emailman.us_temperatures.ui.components.ColorLegend
import edu.emailman.us_temperatures.ui.components.USMapCanvas
import edu.emailman.us_temperatures.viewmodel.DataSource
import edu.emailman.us_temperatures.viewmodel.LoadingState
import edu.emailman.us_temperatures.viewmodel.TemperatureViewModel
import edu.emailman.us_temperatures.viewmodel.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TemperatureViewModel) {
    val cityTemperatures by viewModel.cityTemperatures.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val hoveredCity by viewModel.hoveredCity.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()
    val showGrid by viewModel.showGrid.collectAsState()
    val hasApiKey by viewModel.apiKey.collectAsState()
    val totalCities by viewModel.totalCities.collectAsState()
    val dataSource by viewModel.dataSource.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedStateName by viewModel.selectedStateName.collectAsState()
    val stateDetailState by viewModel.stateDetailState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("US Temperature Map") },
                actions = {
                    // State View toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("State View", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = viewMode == ViewMode.STATE_SELECT,
                            onCheckedChange = {
                                viewModel.setViewMode(if (it) ViewMode.STATE_SELECT else ViewMode.TEMPERATURE_MAP)
                            },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Grid toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Grid", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = showGrid,
                            onCheckedChange = { viewModel.toggleGrid() },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Refresh button (show when API key is configured or data loaded from cache)
                    if (hasApiKey != null || dataSource == DataSource.CACHE) {
                        TextButton(
                            onClick = {
                                if (hasApiKey != null) viewModel.refreshTemperatures()
                                else viewModel.refreshFromCache()
                            },
                            enabled = loadingState !is LoadingState.Loading
                        ) {
                            Text("Refresh")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status bar
            when (val state = loadingState) {
                is LoadingState.NoApiKey -> {
                    // Show prompt to enter API key
                    ApiKeyInputCard(
                        onApiKeySubmit = { viewModel.setApiKey(it) }
                    )
                }
                is LoadingState.Loading -> {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Loading: ${state.loaded}/${state.total} (${(state.progress * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                is LoadingState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    lastUpdated?.let {
                        Text(
                            text = "Last updated: $it",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Main content
            Row(
                modifier = Modifier.fillMaxSize().weight(1f)
            ) {
                // Map canvas
                USMapCanvas(
                    cityTemperatures = cityTemperatures,
                    selectedCity = selectedCity,
                    hoveredCity = hoveredCity,
                    onCitySelected = { viewModel.selectCity(it) },
                    onCityHovered = { viewModel.hoverCity(it) },
                    showGrid = showGrid,
                    viewMode = viewMode,
                    selectedStateName = selectedStateName,
                    stateDetailState = stateDetailState,
                    onStateSelected = { viewModel.selectState(it) },
                    modifier = Modifier.weight(1f)
                )

                // Color legend
                ColorLegend(
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun ApiKeyInputCard(onApiKeySubmit: (String) -> Unit) {
    var inputKey by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Enter OpenWeatherMap API Key",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "To display temperature data, enter your OpenWeatherMap API key. " +
                "Get a free key at openweathermap.org",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it
                        showError = false
                    },
                    label = { Text("API Key") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = showError
                )

                Button(
                    onClick = {
                        if (inputKey.isNotBlank()) {
                            onApiKeySubmit(inputKey.trim())
                        } else {
                            showError = true
                        }
                    }
                ) {
                    Text("Load Data")
                }
            }

            Text(
                "Tip: Set OPENWEATHERMAP_API_KEY environment variable to load automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
