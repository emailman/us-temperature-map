package edu.emailman.us_temperatures.domain

import androidx.compose.ui.graphics.Path

object StatePathConverter {
    /**
     * Convert a StateGeometry to a StateBoundary with screen-coordinate paths
     */
    fun convert(
        state: StateGeometry,
        transformer: CoordinateTransformer
    ): StateBoundary {
        val paths = state.polygons.flatMap { polygon ->
            polygon.map { ring ->
                createPathFromRing(ring, transformer)
            }
        }

        return StateBoundary(
            name = state.name,
            code = state.code,
            paths = paths
        )
    }

    /**
     * Convert a list of StateGeometry to StateBoundary list
     */
    fun convertAll(
        states: List<StateGeometry>,
        transformer: CoordinateTransformer
    ): List<StateBoundary> {
        return states.map { convert(it, transformer) }
    }

    private fun createPathFromRing(
        ring: List<Pair<Double, Double>>,
        transformer: CoordinateTransformer
    ): Path {
        val path = Path()

        if (ring.isEmpty()) return path

        val first = ring.first()
        val startOffset = transformer.toScreenCoords(first.first, first.second)
        path.moveTo(startOffset.x, startOffset.y)

        for (i in 1 until ring.size) {
            val (lat, lon) = ring[i]
            val offset = transformer.toScreenCoords(lat, lon)
            path.lineTo(offset.x, offset.y)
        }

        path.close()
        return path
    }
}
