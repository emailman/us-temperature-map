# US Temperature Map - Project Documentation

## Overview

A Kotlin Multiplatform application displaying real-time temperatures for major US cities across the continental US with precise state boundaries, color-coded temperature readings, interactive city tooltips, and a latitude/longitude grid overlay.

## Current Implementation

### Features
- Pre-cached temperature data loads instantly on startup (via GitHub Actions)
- Real-time temperature data from OpenWeatherMap API (on-demand refresh)
- City-based temperature display (80 major US cities loaded from JSON)
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
- Progressive loading with progress indicator (when fetching from API)
- Refresh button (live API on JVM, re-fetches cache on web)

### Temperature Caching System

A GitHub Actions cron job fetches all city temperatures every 3 hours and commits `dist/temperatures.json`, triggering a Vercel auto-deploy. All targets load this cached data instantly on startup.

**Data flow:**
- **GitHub Actions** (every 3 hours): `scripts/fetch-temperatures.js` -> `dist/temperatures.json` -> git push -> Vercel deploy
- **Web (Vercel)**: Fetches `temperatures.json` from same-origin CDN (cache-busted with random query param)
- **Web (local dev)**: Webpack dev server serves `temperatures.json` from `dist/` via `webpack.config.d/static-files.js`
- **JVM**: Reads `dist/temperatures.json` from local filesystem (checks both `dist/` and `../dist/` for Gradle CWD)
- **Fallback**: If cache unavailable, falls back to API key prompt and live API calls

**Refresh behavior:**
- **JVM** (has API key): Live API fetch for all cities, saves results back to `dist/temperatures.json`
- **Web** (no API key): Re-fetches static `temperatures.json` from server

### Architecture

```
composeApp/src/commonMain/kotlin/edu/emailman/us_temperatures/
├── App.kt                          # Hosts MainScreen
├── data/
│   ├── api/
│   │   ├── OpenWeatherMapApi.kt    # Ktor HTTP client
│   │   └── WeatherResponse.kt      # API response DTOs (includes Wind, Weather)
│   ├── cache/
│   │   └── TemperatureCacheLoader.kt  # expect: load/save cached temperatures
│   ├── geo/
│   │   ├── GeoJsonModels.kt        # GeoJSON serialization models
│   │   ├── GeoJsonParser.kt        # Parse GeoJSON to domain models
│   │   ├── USCitiesData.kt         # Load cities from JSON resource
│   │   └── USStatesGeoData.kt      # Load/cache state boundaries
│   ├── model/
│   │   ├── CachedTemperatureResponse.kt  # Cache JSON DTOs with toTemperatureData()
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
│   └── TemperatureViewModel.kt     # State management (cache-first loading)
└── util/
    ├── Constants.kt                # US bounds, grid config
    └── TimeUtil.kt                 # Time formatting (expect/actual)
```

**Platform-specific actuals:**
```
composeApp/src/{jvmMain,jsMain,wasmJsMain}/kotlin/edu/emailman/us_temperatures/
├── data/cache/
│   └── TemperatureCacheLoader.{jvm,js,wasmJs}.kt  # Platform cache loader
├── util/
│   └── TimeUtil.{jvm,js,wasmJs}.kt                # Platform time formatting
└── main.kt                                         # Platform entry point
```

**Build/deploy files:**
```
.github/workflows/update-temperatures.yml  # Cron job (every 3 hours)
scripts/fetch-temperatures.js              # Node.js fetch script
dist/temperatures.json                     # Generated cached temperature data
composeApp/webpack.config.d/static-files.js  # Dev server static file config
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
- **JVM**: `System.getenv("OPENWEATHERMAP_API_KEY")` (or hardcoded fallback in main.kt)
- **Web**: Cache-first loading requires no API key; URL parameter `?apiKey=xxx` available for override
- **GitHub Actions**: `OPENWEATHERMAP_API_KEY` repository secret

### City Data
- Cities loaded from `composeResources/files/us-cities.json`
- 80 major US cities with name, state, latitude, longitude

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

### Cached Temperature Format (dist/temperatures.json)
```json
{
  "fetchedAt": "2026-02-21 15:38:59",
  "cityCount": 80,
  "temperatures": [
    {
      "latitude": 40.7128, "longitude": -74.006,
      "temperature": 35.2, "locationName": "New York",
      "cityName": "New York", "stateName": "NY",
      "weatherCondition": "Clouds", "weatherDescription": "overcast clouds",
      "humidity": 62, "windSpeed": 8.5, "windDirection": 270,
      "tempMin": 32.1, "tempMax": 38.4
    }
  ]
}
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

# Fetch fresh temperatures (requires OPENWEATHERMAP_API_KEY env var)
node scripts/fetch-temperatures.js
```

## Resources

- `composeResources/files/us-states.geojson` - State boundary data (~89KB)
- `composeResources/files/us-cities.json` - City coordinates (80 cities)
- `dist/temperatures.json` - Cached temperature data (~20KB, auto-generated)
- Source: Natural Earth / PublicaMundi

## Dependencies

- Ktor 3.1.0 - HTTP client
- kotlinx-serialization 1.8.0 - JSON parsing
- kotlinx-datetime 0.6.1 - Date/time handling
- Compose Multiplatform - UI framework

## Known Issues

- `kotlinx-datetime` classes throw `ClassNotFoundException` at JVM runtime; avoid using `Clock.System` or `Instant` in common code that runs on JVM. Use platform-specific time functions via expect/actual instead.
- Ktor WasmJS HTTP client requires **absolute URLs** — relative URLs like `"temperatures.json"` silently fail (the exception is swallowed, returning `null`). Always use `window.location.origin` to build the full URL: `"${window.location.origin}/temperatures.json?t=$cacheBuster"`.
