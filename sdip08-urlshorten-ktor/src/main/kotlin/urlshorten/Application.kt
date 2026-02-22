package urlshorten

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import urlshorten.plugins.*

fun main() {
    embeddedServer(CIO, port = 9999, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureStatusPages()
    configureDatabase()
    configureRouting()
}
