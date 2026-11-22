package dev.nitpick.server

import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.io.path.Path

private const val PORT = 8080
private const val DEFAULT_HOST = "127.0.0.1"

fun main() {
    val file = Path(System.getProperty("user.home"), ".nitpick", "server", "reviews.json")
    val store = ReviewStore(file)
    // Localhost by default: a LAN peer must not read private code snippets. The phone reaches
    // this server through `adb reverse tcp:8080 tcp:8080`; set NITPICK_BIND=0.0.0.0 only for a
    // deliberate LAN demo.
    val host = System.getenv("NITPICK_BIND")?.takeIf { it.isNotBlank() } ?: DEFAULT_HOST
    val server = embeddedServer(CIO, port = PORT, host = host) { reviewModule(store) }
    println("Nitpick review server, reviews stored in $file")
    println("  http://localhost:$PORT")
    if (host == "0.0.0.0") {
        lanAddresses().forEach { println("  http://$it:$PORT") }
    } else {
        println("  Android over USB: adb reverse tcp:$PORT tcp:$PORT")
    }
    server.start(wait = true)
}

private fun lanAddresses(): List<String> =
    NetworkInterface.getNetworkInterfaces().asSequence()
        .filter { it.isUp && !it.isLoopback }
        .flatMap { it.inetAddresses.asSequence() }
        .filterIsInstance<Inet4Address>()
        .map { it.hostAddress }
        .toList()
