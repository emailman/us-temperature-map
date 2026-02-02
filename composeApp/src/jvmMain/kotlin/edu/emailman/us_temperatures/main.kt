package edu.emailman.us_temperatures

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import edu.emailman.us_temperatures.viewmodel.TemperatureViewModel

fun main() = application {
    val envApiKey = System.getenv("OPENWEATHERMAP_API_KEY")
    val viewModel = remember { TemperatureViewModel(envApiKey) }

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
