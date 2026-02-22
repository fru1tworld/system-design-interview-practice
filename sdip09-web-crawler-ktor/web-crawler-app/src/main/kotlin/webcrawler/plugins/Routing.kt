package webcrawler.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import webcrawler.domain.urldiscovery.urlDiscoveryRoutes

fun Application.configureRouting() {
    routing {
        urlDiscoveryRoutes()
    }
}
