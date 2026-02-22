package htmlparser

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO, port = 10003, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    log.info("HTML Parser application started on port 10003")
    // TODO: Implement dynamic HTML parsing with Selenium/Playwright
}
