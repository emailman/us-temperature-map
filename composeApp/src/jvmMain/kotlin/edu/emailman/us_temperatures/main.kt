package edu.emailman.us_temperatures

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import edu.emailman.us_temperatures.viewmodel.TemperatureViewModel

private const val API_KEY = "f282e6810344a266faf4e7311d63359d"

fun main() = application {
    val apiKey = System.getenv("OPENWEATHERMAP_API_KEY") ?: API_KEY
    val viewModel = remember { TemperatureViewModel(apiKey) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "US Temperature Map",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        MaterialTheme {
            App(viewModel)
        }
    }
}
