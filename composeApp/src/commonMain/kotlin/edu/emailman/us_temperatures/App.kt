package edu.emailman.us_temperatures

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import edu.emailman.us_temperatures.ui.MainScreen
import edu.emailman.us_temperatures.viewmodel.TemperatureViewModel

@Composable
fun App(viewModel: TemperatureViewModel) {
    MaterialTheme {
        MainScreen(viewModel)
    }
}
