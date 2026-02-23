package edu.emailman.us_temperatures.data.cache

import edu.emailman.us_temperatures.data.model.CachedTemperatureResponse

expect suspend fun loadCachedTemperatures(): CachedTemperatureResponse?

expect fun saveCachedTemperatures(response: CachedTemperatureResponse)
