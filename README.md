# US Temperature Map

A Kotlin Multiplatform application that displays real-time temperatures across the continental United States on an interactive map with state boundaries.

![US Temperature Map Screenshot](screenshot.png)

## Features

- **Real-time temperature data** from OpenWeatherMap API
- **Precise state boundaries** rendered from GeoJSON data (48 continental states)
- **Color-coded temperatures** with gradient from blue (cold) to red (hot)
- **5° x 5° grid overlay** with latitude/longitude labels
- **Progressive loading** with progress indicator
- **Cross-platform** - runs on Desktop (JVM) and Web (Wasm/JS)

## Requirements

- JDK 17 or higher
- OpenWeatherMap API key (free tier works)

## Getting Started

### Set up API Key

Set the `OPENWEATHERMAP_API_KEY` environment variable:

```shell
# Windows
set OPENWEATHERMAP_API_KEY=your_api_key_here

# macOS/Linux
export OPENWEATHERMAP_API_KEY=your_api_key_here
```

Or enter the API key in the app when prompted.

### Run Desktop Application

```shell
# Windows
.\gradlew.bat :composeApp:run

# macOS/Linux
./gradlew :composeApp:run
```

### Run Web Application

```shell
# Wasm target (faster, modern browsers)
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun

# JS target (supports older browsers)
.\gradlew.bat :composeApp:jsBrowserDevelopmentRun
```

## Architecture

```
composeApp/src/commonMain/kotlin/edu/emailman/us_temperatures/
├── App.kt                          # Main app entry point
├── data/
│   ├── api/                        # OpenWeatherMap API client
│   ├── geo/                        # GeoJSON parsing for state boundaries
│   ├── model/                      # Data models
│   └── repository/                 # Weather data repository
├── domain/
│   ├── CoordinateTransformer.kt    # Lat/lon to screen coordinates
│   ├── StateGeometry.kt            # State boundary models
│   ├── StatePathConverter.kt       # Convert coordinates to paths
│   └── TemperatureColorMapper.kt   # Temperature to color mapping
├── ui/
│   ├── components/
│   │   ├── ColorLegend.kt          # Temperature scale sidebar
│   │   ├── GridOverlay.kt          # Lat/lon grid lines
│   │   ├── HeatMapRenderer.kt      # Temperature display
│   │   ├── StateBoundariesRenderer.kt  # State/national borders
│   │   └── USMapCanvas.kt          # Main map canvas
│   └── MainScreen.kt               # Main UI layout
├── viewmodel/
│   └── TemperatureViewModel.kt     # State management
└── util/
    └── Constants.kt                # Configuration constants
```

## Configuration

Grid and display settings in `Constants.kt`:

| Parameter | Value |
|-----------|-------|
| Latitude range | 25°N - 49°N |
| Longitude range | 66°W - 125°W |
| Grid spacing | 5° x 5° |
| Temperature range | -20°F to 100°F |

## Technologies

- **Kotlin Multiplatform** - Cross-platform development
- **Compose Multiplatform** - Declarative UI framework
- **Ktor** - HTTP client for API calls
- **kotlinx.serialization** - JSON parsing
- **GeoJSON** - State boundary data

## License

MIT License
