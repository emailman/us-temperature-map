# US Temperature Map - Project Documentation

## Overview

A Kotlin Multiplatform application displaying real-time temperatures for major US cities across the continental US with precise state boundaries, color-coded temperature readings, interactive city tooltips, and a latitude/longitude grid overlay.

## Current Implementation

### Features
- Real-time temperature data from OpenWeatherMap API
- City-based temperature display (100 major US cities loaded from JSON)
- Interactive tooltips on hover (preview) or click/tap (pin) showing:
  - City name and state
  - Current temperature
  - Weather description
  - High/low temperatures
  - Humidity percentage
  - Wind speed with cardinal direction (N, NE, E, SE, S, SW, W, NW)
- Precise GeoJSON-based state boundaries (48 continental states)
- Temperature readings displayed as colored circles with white text
- 5° x 5° grid with dashed lines and labels (toggleable)
- Progressive loading with progress indicator
- Refresh button

### Architecture

```
composeApp/src/commonMain/kotlin/edu/emailman/us_temperatures/
├── App.kt                          # Hosts MainScreen
├── data/
│   ├── api/
│   │   ├── OpenWeatherMapApi.kt    # Ktor HTTP client
│   │   └── WeatherResponse.kt      # API response DTOs (includes Wind, Weather)
│   ├── geo/
│   │   ├── GeoJsonModels.kt        # GeoJSON serialization models
│   │   ├── GeoJsonParser.kt        # Parse GeoJSON to domain models
│   │   ├── USCitiesData.kt         # Load cities from JSON resource
│   │   └── USStatesGeoData.kt      # Load/cache state boundaries
│   ├── model/
│   │   ├── City.kt                 # City data model
│   │   └── TemperatureData.kt      # Temperature domain model with weather details
│   └── repository/
│       └── CityWeatherRepository.kt # City-based weather data fetching
├── domain/
│   ├── CoordinateTransformer.kt    # Lat/lon to screen coordinates
│   ├── StateGeometry.kt            # State boundary domain models
│   ├── StatePathConverter.kt       # Convert to Compose Paths
│   ├── TemperatureColorMapper.kt   # Temperature to color gradient
│   └── USMapData.kt                # Legacy simplified US outline
├── ui/
│   ├── components/
│   │   ├── CityTooltip.kt          # City weather detail popup
│   │   ├── ColorLegend.kt          # Temperature scale sidebar
│   │   ├── GridOverlay.kt          # Lat/lon grid drawing
│   │   ├── HeatMapRenderer.kt      # City marker rendering
│   │   ├── StateBoundariesRenderer.kt  # State/national borders
│   │   └── USMapCanvas.kt          # Main interactive Canvas composable
│   ├── model/
│   │   └── CityDisplayData.kt      # Screen coordinates for hit testing
│   └── MainScreen.kt               # Main layout with controls
├── viewmodel/
│   └── TemperatureViewModel.kt     # State management
└── util/
    ├── Constants.kt                # US bounds, grid config
    └── TimeUtil.kt                 # Time formatting
```

### Configuration (Constants.kt)

| Parameter | Value | Description |
|-----------|-------|-------------|
| US_LAT_MIN | 25.0 | Southern boundary |
| US_LAT_MAX | 49.0 | Northern boundary |
| US_LON_MIN | -125.0 | Western boundary |
| US_LON_MAX | -66.0 | Eastern boundary |
| GRID_LAT_SPACING | 5.0 | Display grid latitude spacing |
| GRID_LON_SPACING | 5.0 | Display grid longitude spacing |
| TEMP_MIN | 0.0 | Minimum temperature for color scale |
| TEMP_MAX | 100.0 | Maximum temperature for color scale |

### Rendering Layer Order (bottom to top)
1. Light gray background (#D3D3D3)
2. City temperature markers (white text with color-coded circles)
3. State borders (thin gray, 0.5px)
4. National border (thick dark green, 2px)
5. Grid overlay (dashed lines with labels)
6. City tooltip (when city hovered or selected)

### Temperature Colors
| Temp (°F) | Color |
|-----------|-------|
| -20 | Deep Blue (#0000FF) |
| 20 | Royal Blue (#4169E1) |
| 32 | Green (#00FF00) |
| 50 | Yellow (#FFFF00) |
| 70 | Orange (#FFA500) |
| 85 | Orange-Red (#FF4500) |
| 100+ | Crimson (#DC143C) |

### API Key Handling
- **JVM**: `System.getenv("OPENWEATHERMAP_API_KEY")`
- **Web**: Enter in UI prompt or URL parameter `?apiKey=xxx`

### City Data
- Cities loaded from `composeResources/files/us-cities.json`
- 100 major US cities with name, state, latitude, longitude
- Total: 100 API calls per refresh (rate limited to ~55/minute)

### TemperatureData Model
```kotlin
data class TemperatureData(
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val locationName: String,
    val cityName: String,
    val stateName: String,
    val weatherCondition: String,
    val weatherDescription: String,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val tempMin: Double,
    val tempMax: Double
)
```

## Build Commands

```shell
# Desktop app
./gradlew.bat :composeApp:run

# Web (Wasm)
./gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun

# Web (JS)
./gradlew.bat :composeApp:jsBrowserDevelopmentRun

# Compile check
./gradlew.bat :composeApp:compileKotlinJvm
```

## Resources

- `composeResources/files/us-states.geojson` - State boundary data (~89KB)
- `composeResources/files/us-cities.json` - City coordinates (100 cities)
- Source: Natural Earth / PublicaMundi

## Dependencies

- Ktor 3.1.0 - HTTP client
- kotlinx-serialization 1.8.0 - JSON parsing
- kotlinx-datetime 0.6.1 - Date/time handling
- Compose Multiplatform - UI framework
