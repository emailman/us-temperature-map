package edu.emailman.us_temperatures.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.emailman.us_temperatures.data.api.OpenWeatherMapApi
import edu.emailman.us_temperatures.data.cache.loadCachedTemperatures
import edu.emailman.us_temperatures.data.cache.saveCachedTemperatures
import edu.emailman.us_temperatures.data.model.CachedTemperatureEntry
import edu.emailman.us_temperatures.data.model.CachedTemperatureResponse
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

enum class DataSource { NONE, CACHE, API }

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

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    private val _lastUpdated = MutableStateFlow<String?>(null)
    val lastUpdated: StateFlow<String?> = _lastUpdated.asStateFlow()

    private val _showGrid = MutableStateFlow(true)
    val showGrid: StateFlow<Boolean> = _showGrid.asStateFlow()

    private val _totalCities = MutableStateFlow(0)
    val totalCities: StateFlow<Int> = _totalCities.asStateFlow()

    private val _dataSource = MutableStateFlow(DataSource.NONE)
    val dataSource: StateFlow<DataSource> = _dataSource.asStateFlow()

    init {
        viewModelScope.launch {
            cities = USCitiesData.loadCities()
            _totalCities.value = cities.size

            // Try loading cached temperatures first
            val cached = try {
                loadCachedTemperatures()
            } catch (_: Exception) {
                null
            }

            // Initialize API client if key is available
            if (!initialApiKey.isNullOrBlank()) {
                api = OpenWeatherMapApi(initialApiKey)
                repository = CityWeatherRepository(api!!)
            }

            if (cached != null && cached.temperatures.isNotEmpty()) {
                _cityTemperatures.value = cached.temperatures.map { it.toTemperatureData() }
                _dataSource.value = DataSource.CACHE
                _lastUpdated.value = "Cached: ${cached.fetchedAt} UTC"
                _loadingState.value = LoadingState.Success
            } else if (repository != null) {
                refreshTemperatures()
            } else {
                _loadingState.value = LoadingState.NoApiKey
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

                // Save to cache file
                val timeString = getCurrentTimeString()
                saveCachedTemperatures(CachedTemperatureResponse(
                    fetchedAt = timeString,
                    cityCount = _cityTemperatures.value.size,
                    temperatures = _cityTemperatures.value.map { t ->
                        CachedTemperatureEntry(
                            latitude = t.latitude,
                            longitude = t.longitude,
                            temperature = t.temperature,
                            locationName = t.locationName,
                            cityName = t.cityName,
                            stateName = t.stateName,
                            weatherCondition = t.weatherCondition,
                            weatherDescription = t.weatherDescription,
                            humidity = t.humidity,
                            windSpeed = t.windSpeed,
                            windDirection = t.windDirection,
                            tempMin = t.tempMin,
                            tempMax = t.tempMax
                        )
                    }
                ))

                _dataSource.value = DataSource.API
                _loadingState.value = LoadingState.Success
                _lastUpdated.value = timeString
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refreshFromCache() {
        viewModelScope.launch {
            val cached = try {
                loadCachedTemperatures()
            } catch (_: Exception) {
                null
            }

            if (cached != null && cached.temperatures.isNotEmpty()) {
                _cityTemperatures.value = cached.temperatures.map { it.toTemperatureData() }
                _dataSource.value = DataSource.CACHE
                _lastUpdated.value = "Cached: ${cached.fetchedAt} UTC"
                _loadingState.value = LoadingState.Success
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
