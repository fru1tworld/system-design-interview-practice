package webcrawler.domain.urldiscovery

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import webcrawler.plugins.kafkaProducer

fun Route.urlDiscoveryRoutes() {
    val producer = UrlDiscoveryProducer(application.kafkaProducer)
    val service = UrlDiscoveryService(producer)

    route("/api/v1/url-discovery") {
        post {
            val requestDto = call.receive<UrlDiscoveryRequestDto>()
            val response = service.createUrlDiscovery(requestDto)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/batch") {
            val requestDtos = call.receive<List<UrlDiscoveryRequestDto>>()
            val response = service.createUrlDiscoveries(requestDtos)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
