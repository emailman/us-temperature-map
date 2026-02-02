package edu.emailman.us_temperatures.util

import kotlin.js.Date

actual fun getCurrentTimeString(): String {
    val date = Date()
    return date.toISOString().substringBefore('.')
}
