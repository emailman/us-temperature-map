package edu.emailman.us_temperatures.domain

object PointInPolygon {

    fun isPointInRing(lat: Double, lon: Double, ring: List<Pair<Double, Double>>): Boolean {
        var inside = false
        var j = ring.size - 1
        for (i in ring.indices) {
            val (latI, lonI) = ring[i]
            val (latJ, lonJ) = ring[j]
            if ((lonI > lon) != (lonJ > lon) &&
                lat < (latJ - latI) * (lon - lonI) / (lonJ - lonI) + latI
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    // polygon[0] = exterior ring, polygon[1+] = holes
    fun isPointInPolygon(lat: Double, lon: Double, polygon: List<List<Pair<Double, Double>>>): Boolean {
        if (polygon.isEmpty()) return false
        if (!isPointInRing(lat, lon, polygon[0])) return false
        return polygon.drop(1).none { isPointInRing(lat, lon, it) }
    }

    fun isPointInState(lat: Double, lon: Double, state: StateGeometry): Boolean =
        state.polygons.any { isPointInPolygon(lat, lon, it) }
}
