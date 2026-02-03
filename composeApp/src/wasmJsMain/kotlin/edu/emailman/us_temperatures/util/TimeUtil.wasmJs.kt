@file:OptIn(kotlin.js.ExperimentalJsExport::class)
@file:Suppress("OPT_IN_USAGE")

package edu.emailman.us_temperatures.util

private fun getYear(): Int = js("new Date().getFullYear()")
private fun getMonth(): Int = js("new Date().getMonth() + 1")
private fun getDay(): Int = js("new Date().getDate()")
private fun getHours(): Int = js("new Date().getHours()")
private fun getMinutes(): Int = js("new Date().getMinutes()")
private fun getSeconds(): Int = js("new Date().getSeconds()")

actual fun getCurrentTimeString(): String {
    val year = getYear()
    val month = getMonth().toString().padStart(2, '0')
    val day = getDay().toString().padStart(2, '0')
    val hours = getHours().toString().padStart(2, '0')
    val minutes = getMinutes().toString().padStart(2, '0')
    val seconds = getSeconds().toString().padStart(2, '0')
    return "$year-$month-$day $hours:$minutes:$seconds"
}
