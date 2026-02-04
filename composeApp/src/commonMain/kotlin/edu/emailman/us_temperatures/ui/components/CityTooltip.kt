package edu.emailman.us_temperatures.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.emailman.us_temperatures.data.model.TemperatureData
import kotlin.math.roundToInt

private fun degreesToCardinal(degrees: Int): String {
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = ((degrees + 22.5) / 45).toInt() % 8
    return directions[index]
}

@Composable
fun CityTooltip(
    city: TemperatureData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "${city.cityName}, ${city.stateName}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${city.temperature.roundToInt()}°F",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (city.weatherDescription.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = city.weatherDescription.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "High: ${city.tempMax.roundToInt()}° / Low: ${city.tempMin.roundToInt()}°",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Humidity: ${city.humidity}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val windText = if (city.windSpeed.roundToInt() > 0) {
                "Wind: ${city.windSpeed.roundToInt()} mph ${degreesToCardinal(city.windDirection)}"
            } else {
                "Wind: 0 mph"
            }
            Text(
                text = windText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
