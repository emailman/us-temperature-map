package edu.emailman.us_temperatures

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform