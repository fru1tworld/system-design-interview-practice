package webcrawler

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import webcrawler.plugins.configureRouting
import webcrawler.plugins.configureSerialization
import webcrawler.plugins.configureStatusPages
import webcrawler.plugins.configureKafka

fun main() {
    embeddedServer(CIO, port = 10000, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureStatusPages()
    configureKafka()
    configureRouting()
}
