# US Temperature Map - Project Documentation

## Overview

A Kotlin Multiplatform application displaying real-time temperatures across the continental US with precise state boundaries, color-coded temperature readings, and a latitude/longitude grid overlay.

## Current Implementation

### Features
- Real-time temperature data from OpenWeatherMap API
- Precise GeoJSON-based state boundaries (48 continental states)
- Temperature readings displayed as colored text with circles at grid centers
- 5° x 5° grid with dashed lines and labels
- Progressive loading with progress indicator
- Grid toggle switch
- Refresh button

### Architecture

```
composeApp/src/commonMain/kotlin/edu/emailman/us_temperatures/
├── App.kt                          # Hosts MainScreen
├── data/
│   ├── api/
│   │   ├── OpenWeatherMapApi.kt    # Ktor HTTP client
│   │   └── WeatherResponse.kt      # API response DTOs
│   ├── geo/
│   │   ├── GeoJsonModels.kt        # GeoJSON serialization models
│   │   ├── GeoJsonParser.kt        # Parse GeoJSON to domain models
│   │   └── USStatesGeoData.kt      # Load/cache state boundaries
│   ├── model/
│   │   └── TemperatureData.kt      # Temperature domain model
│   └── repository/
│       └── WeatherRepository.kt    # Grid-based data fetching
├── domain/
│   ├── CoordinateTransformer.kt    # Lat/lon to screen coordinates
│   ├── StateGeometry.kt            # State boundary domain models
│   ├── StatePathConverter.kt       # Convert to Compose Paths
│   ├── TemperatureColorMapper.kt   # Temperature to color gradient
│   └── USMapData.kt                # Legacy simplified US outline
├── ui/
│   ├── components/
│   │   ├── ColorLegend.kt          # Temperature scale sidebar
│   │   ├── GridOverlay.kt          # Lat/lon grid drawing
│   │   ├── HeatMapRenderer.kt      # Temperature text + circles
│   │   ├── StateBoundariesRenderer.kt  # State/national borders
│   │   └── USMapCanvas.kt          # Main Canvas composable
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
| GRID_LAT_SPACING | 5.0 | Data/display grid latitude spacing |
| GRID_LON_SPACING | 5.0 | Data/display grid longitude spacing |
| TEMP_MIN | 0.0 | Minimum temperature for color scale |
| TEMP_MAX | 100.0 | Maximum temperature for color scale |

### Rendering Layer Order (bottom to top)
1. Light gray background (#D3D3D3)
2. Temperature readings (white text with color-coded circles)
3. State borders (thin gray, 0.5px)
4. National border (thick dark green, 2px)
5. Grid overlay (dashed lines with labels)

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

### Grid Points
- Points generated at cell centers (e.g., 27.5°, 32.5°, etc.)
- Each reading represents temperature at the center of a 5° x 5° cell
- Total: ~60 API calls per refresh

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
- Source: Natural Earth / PublicaMundi

## Dependencies

- Ktor 3.1.0 - HTTP client
- kotlinx-serialization 1.8.0 - JSON parsing
- kotlinx-datetime 0.6.1 - Date/time handling
- Compose Multiplatform - UI framework

---

## Planned: City-Based Temperature Display

### Overview
Change from displaying temperatures at grid centers (5° intervals, ~60 points) to displaying temperatures for 50-100 specific US cities loaded from a JSON file, with interactive tooltips showing city details on hover/click.

### Requirements
- Load city list from JSON resource file (50-100 cities)
- Display temperature inside colored circles (current visual style)
- Interactive: hover/click shows popup with city name, temperature, and weather conditions

### Implementation Steps

#### Phase 1: Data Layer

**1.1 Add Weather class to WeatherResponse**
- File: `data/api/WeatherResponse.kt`
- Add `Weather` data class with `main` (condition) and `description` fields
- Add `weather: List<Weather>` to `WeatherResponse`

**1.2 Create City data model**
- New file: `data/model/City.kt`
- Fields: `name`, `state`, `latitude`, `longitude`

**1.3 Expand TemperatureData model**
- File: `data/model/TemperatureData.kt`
- Add: `cityName`, `stateName`, `weatherCondition`, `weatherDescription`

**1.4 Create cities JSON file**
- New file: `composeResources/files/us-cities.json`
- Include 50-100 major US cities

**1.5 Create city data loader**
- New file: `data/geo/USCitiesData.kt`
- Follow `USStatesGeoData.kt` pattern

#### Phase 2: Repository Layer

**2.1 Create CityWeatherRepository**
- New file: `data/repository/CityWeatherRepository.kt`
- Accept `List<City>` instead of generating grid points
- Include weather conditions in emitted `TemperatureData`

#### Phase 3: ViewModel Changes

**3.1 Update TemperatureViewModel**
- File: `viewmodel/TemperatureViewModel.kt`
- Change from `Map<Pair<Double, Double>, Double>` to `List<TemperatureData>`
- Load cities on init via `USCitiesData.loadCities()`
- Add `selectedCity` state for hover/click

#### Phase 4: UI Layer

**4.1 Create CityDisplayData for hit testing**
- New file: `ui/model/CityDisplayData.kt`
- Store screen coordinates and radius for hit testing

**4.2 Update HeatMapRenderer**
- File: `ui/components/HeatMapRenderer.kt`
- Add `drawCityMarkers()` - draw at exact coordinates (no grid cell math)
- Return `List<CityDisplayData>` for hit testing

**4.3 Create CityTooltip component**
- New file: `ui/components/CityTooltip.kt`
- Card showing: city name + state, temperature, weather condition

**4.4 Update USMapCanvas for interactivity**
- File: `ui/components/USMapCanvas.kt`
- Change signature to accept `List<TemperatureData>`, `selectedCity`, `onCitySelected`
- Add pointer input for tap/hover detection
- Render tooltip overlay

**4.5 Update MainScreen**
- File: `ui/MainScreen.kt`
- Collect `cityTemperatures` and `selectedCity` from ViewModel

### Files Summary

**New Files (6):**
- `data/model/City.kt`
- `data/geo/USCitiesData.kt`
- `data/repository/CityWeatherRepository.kt`
- `ui/model/CityDisplayData.kt`
- `ui/components/CityTooltip.kt`
- `composeResources/files/us-cities.json`

**Modified Files (6):**
- `data/api/WeatherResponse.kt`
- `data/model/TemperatureData.kt`
- `viewmodel/TemperatureViewModel.kt`
- `ui/components/HeatMapRenderer.kt`
- `ui/components/USMapCanvas.kt`
- `ui/MainScreen.kt`

### Verification
1. Build check: `./gradlew.bat :composeApp:compileKotlinJvm`
2. Desktop test: `./gradlew.bat :composeApp:run`
3. Web test: `./gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun`
