package edu.emailman.us_temperatures.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.emailman.us_temperatures.data.model.TemperatureData
import kotlin.math.roundToInt

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

            if (city.weatherCondition.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = city.weatherCondition,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (city.weatherDescription.isNotEmpty() && city.weatherDescription != city.weatherCondition) {
                    Text(
                        text = city.weatherDescription.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
