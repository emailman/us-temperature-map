package edu.emailman.us_temperatures.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.emailman.us_temperatures.domain.TemperatureColorMapper

@Composable
fun ColorLegend(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(80.dp)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "°F",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxHeight().weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Temperature labels on the left
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val temps = listOf(100, 85, 70, 50, 32, 20, 0, -20)
                temps.forEach { temp ->
                    Text(
                        text = "$temp",
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            // Gradient bar
            Canvas(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
            ) {
                val gradientColors = listOf(
                    Color(0xFF8B0000),  // 120°F Dark Red
                    Color(0xFFDC143C),  // 100°F Crimson
                    Color(0xFFFF4500),  // 85°F Orange-Red
                    Color(0xFFFFA500),  // 70°F Orange
                    Color(0xFFFFFF00),  // 50°F Yellow
                    Color(0xFF00FF00),  // 32°F Green
                    Color(0xFF4169E1),  // 20°F Royal Blue
                    Color(0xFF0066FF),  // 0°F Blue
                    Color(0xFF0000FF)   // -20°F Deep Blue
                )

                val brush = Brush.verticalGradient(gradientColors)
                drawRect(
                    brush = brush,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height)
                )

                // Draw border
                drawRect(
                    color = Color.Gray,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                )
            }
        }
    }
}
