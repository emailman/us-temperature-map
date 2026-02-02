package edu.emailman.us_temperatures.data.geo

import edu.emailman.us_temperatures.domain.StateGeometry
import us_temperatures.composeapp.generated.resources.Res

object USStatesGeoData {
    private var cachedStates: List<StateGeometry>? = null

    suspend fun loadStates(): List<StateGeometry> {
        cachedStates?.let { return it }

        val bytes = Res.readBytes("files/us-states.geojson")
        val geoJsonString = bytes.decodeToString()
        val states = GeoJsonParser.parse(geoJsonString)

        cachedStates = states
        return states
    }

    fun getCachedStates(): List<StateGeometry>? = cachedStates
}
