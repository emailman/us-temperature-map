package edu.emailman.us_temperatures.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.emailman.us_temperatures.data.api.OpenWeatherMapApi
import edu.emailman.us_temperatures.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import edu.emailman.us_temperatures.util.getCurrentTimeString

sealed class LoadingState {
    object Idle : LoadingState()
    object NoApiKey : LoadingState()
    data class Loading(val progress: Float, val loaded: Int, val total: Int) : LoadingState()
    object Success : LoadingState()
    data class Error(val message: String) : LoadingState()
}

class TemperatureViewModel(initialApiKey: String? = null) : ViewModel() {
    private var api: OpenWeatherMapApi? = null
    private var repository: WeatherRepository? = null

    private val _apiKey = MutableStateFlow(initialApiKey?.takeIf { it.isNotBlank() })
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    val hasApiKey: Boolean get() = _apiKey.value != null

    private val _temperatures = MutableStateFlow<Map<Pair<Double, Double>, Double>>(emptyMap())
    val temperatures: StateFlow<Map<Pair<Double, Double>, Double>> = _temperatures.asStateFlow()

    private val _loadingState = MutableStateFlow<LoadingState>(
        if (initialApiKey.isNullOrBlank()) LoadingState.NoApiKey else LoadingState.Idle
    )
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    private val _lastUpdated = MutableStateFlow<String?>(null)
    val lastUpdated: StateFlow<String?> = _lastUpdated.asStateFlow()

    private val _showGrid = MutableStateFlow(true)
    val showGrid: StateFlow<Boolean> = _showGrid.asStateFlow()

    val totalGridPoints: Int = WeatherRepository.generateGridPoints().size

    init {
        if (!initialApiKey.isNullOrBlank()) {
            api = OpenWeatherMapApi(initialApiKey)
            repository = WeatherRepository(api!!)
            refreshTemperatures()
        }
    }

    fun setApiKey(newApiKey: String) {
        if (newApiKey.isBlank()) return

        // Close existing API client if any
        api?.close()

        _apiKey.value = newApiKey
        api = OpenWeatherMapApi(newApiKey)
        repository = WeatherRepository(api!!)
        refreshTemperatures()
    }

    fun refreshTemperatures() {
        val repo = repository ?: run {
            _loadingState.value = LoadingState.NoApiKey
            return
        }

        viewModelScope.launch {
            _temperatures.value = emptyMap()
            _loadingState.value = LoadingState.Loading(0f, 0, totalGridPoints)

            var loaded = 0

            try {
                repo.fetchTemperaturesProgressively().collect { tempData ->
                    loaded++
                    if (!tempData.temperature.isNaN()) {
                        _temperatures.update { current ->
                            current + (Pair(tempData.latitude, tempData.longitude) to tempData.temperature)
                        }
                    }
                    _loadingState.value = LoadingState.Loading(
                        progress = loaded.toFloat() / totalGridPoints,
                        loaded = loaded,
                        total = totalGridPoints
                    )
                }

                _loadingState.value = LoadingState.Success
                _lastUpdated.value = getCurrentTimeString()
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleGrid() {
        _showGrid.value = !_showGrid.value
    }

    override fun onCleared() {
        super.onCleared()
        api?.close()
    }
}
