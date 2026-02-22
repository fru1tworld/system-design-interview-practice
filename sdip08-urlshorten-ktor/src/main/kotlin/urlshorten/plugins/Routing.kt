package urlshorten.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import urlshorten.domain.shortenurl.shortenUrlRoutes

fun Application.configureRouting() {
    routing {
        shortenUrlRoutes()
    }
}
