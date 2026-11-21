package dev.nitpick.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform