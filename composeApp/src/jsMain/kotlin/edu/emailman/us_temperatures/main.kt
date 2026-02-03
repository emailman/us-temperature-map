package edu.emailman.us_temperatures

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import edu.emailman.us_temperatures.viewmodel.TemperatureViewModel
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Get API key from URL parameter: ?apiKey=xxx (optional)
    val urlParams = window.location.search
    val apiKey = parseUrlParam(urlParams, "apiKey")

    val viewModel = TemperatureViewModel(apiKey)

    ComposeViewport(document.body!!) {
        App(viewModel)
    }
}

private fun parseUrlParam(search: String, param: String): String? {
    if (search.isEmpty() || !search.startsWith("?")) return null
    val params = search.substring(1).split("&")
    for (p in params) {
        val parts = p.split("=")
        if (parts.size == 2 && parts[0] == param) {
            return parts[1]
        }
    }
    return null
}
