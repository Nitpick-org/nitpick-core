package dev.nitpick.server

import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.io.path.Path

private const val PORT = 8080

fun main() {
    val file = Path(System.getProperty("user.home"), ".nitpick", "server", "reviews.json")
    val store = ReviewStore(file)
    // 0.0.0.0 so a phone reaches the laptop over the LAN, or through `adb reverse tcp:8080 tcp:8080`.
    val server = embeddedServer(CIO, port = PORT, host = "0.0.0.0") { reviewModule(store) }
    println("Nitpick review server, reviews stored in $file")
    println("  http://localhost:$PORT")
    lanAddresses().forEach { println("  http://$it:$PORT") }
    server.start(wait = true)
}

private fun lanAddresses(): List<String> =
    NetworkInterface.getNetworkInterfaces().asSequence()
        .filter { it.isUp && !it.isLoopback }
        .flatMap { it.inetAddresses.asSequence() }
        .filterIsInstance<Inet4Address>()
        .map { it.hostAddress }
        .toList()
