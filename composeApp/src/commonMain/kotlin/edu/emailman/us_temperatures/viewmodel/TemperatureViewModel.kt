package edu.emailman.us_temperatures.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.emailman.us_temperatures.data.api.OpenWeatherMapApi
import edu.emailman.us_temperatures.data.geo.USCitiesData
import edu.emailman.us_temperatures.data.model.City
import edu.emailman.us_temperatures.data.model.TemperatureData
import edu.emailman.us_temperatures.data.repository.CityWeatherRepository
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
    private var repository: CityWeatherRepository? = null
    private var cities: List<City> = emptyList()

    private val _apiKey = MutableStateFlow(initialApiKey?.takeIf { it.isNotBlank() })
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    val hasApiKey: Boolean get() = _apiKey.value != null

    private val _cityTemperatures = MutableStateFlow<List<TemperatureData>>(emptyList())
    val cityTemperatures: StateFlow<List<TemperatureData>> = _cityTemperatures.asStateFlow()

    private val _selectedCity = MutableStateFlow<TemperatureData?>(null)
    val selectedCity: StateFlow<TemperatureData?> = _selectedCity.asStateFlow()

    private val _hoveredCity = MutableStateFlow<TemperatureData?>(null)
    val hoveredCity: StateFlow<TemperatureData?> = _hoveredCity.asStateFlow()

    private val _loadingState = MutableStateFlow<LoadingState>(
        if (initialApiKey.isNullOrBlank()) LoadingState.NoApiKey else LoadingState.Idle
    )
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    private val _lastUpdated = MutableStateFlow<String?>(null)
    val lastUpdated: StateFlow<String?> = _lastUpdated.asStateFlow()

    private val _showGrid = MutableStateFlow(true)
    val showGrid: StateFlow<Boolean> = _showGrid.asStateFlow()

    private val _totalCities = MutableStateFlow(0)
    val totalCities: StateFlow<Int> = _totalCities.asStateFlow()

    init {
        viewModelScope.launch {
            cities = USCitiesData.loadCities()
            _totalCities.value = cities.size

            if (!initialApiKey.isNullOrBlank()) {
                api = OpenWeatherMapApi(initialApiKey)
                repository = CityWeatherRepository(api!!)
                refreshTemperatures()
            }
        }
    }

    fun setApiKey(newApiKey: String) {
        if (newApiKey.isBlank()) return

        // Close existing API client if any
        api?.close()

        _apiKey.value = newApiKey
        api = OpenWeatherMapApi(newApiKey)
        repository = CityWeatherRepository(api!!)
        refreshTemperatures()
    }

    fun refreshTemperatures() {
        val repo = repository ?: run {
            _loadingState.value = LoadingState.NoApiKey
            return
        }

        if (cities.isEmpty()) {
            _loadingState.value = LoadingState.Error("Cities not loaded")
            return
        }

        viewModelScope.launch {
            _cityTemperatures.value = emptyList()
            _selectedCity.value = null
            _loadingState.value = LoadingState.Loading(0f, 0, cities.size)

            var loaded = 0

            try {
                repo.fetchCityTemperaturesProgressively(cities).collect { tempData ->
                    loaded++
                    if (!tempData.temperature.isNaN()) {
                        _cityTemperatures.update { current ->
                            current + tempData
                        }
                    }
                    _loadingState.value = LoadingState.Loading(
                        progress = loaded.toFloat() / cities.size,
                        loaded = loaded,
                        total = cities.size
                    )
                }

                _loadingState.value = LoadingState.Success
                _lastUpdated.value = getCurrentTimeString()
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun selectCity(city: TemperatureData?) {
        _selectedCity.value = city
    }

    fun hoverCity(city: TemperatureData?) {
        _hoveredCity.value = city
    }

    fun toggleGrid() {
        _showGrid.value = !_showGrid.value
    }

    override fun onCleared() {
        super.onCleared()
        api?.close()
    }
}
