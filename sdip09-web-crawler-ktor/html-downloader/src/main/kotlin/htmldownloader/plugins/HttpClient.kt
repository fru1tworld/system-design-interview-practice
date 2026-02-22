package htmldownloader.plugins

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.server.application.*

private lateinit var httpClientInstance: HttpClient

val Application.httpClient: HttpClient
    get() = httpClientInstance

fun Application.configureHttpClient() {
    httpClientInstance = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }

        engine {
            maxConnectionsCount = 100
            endpoint {
                maxConnectionsPerRoute = 20
                pipelineMaxSize = 20
                keepAliveTime = 5000
                connectTimeout = 10000
                connectAttempts = 3
            }
        }
    }

    environment.monitor.subscribe(ApplicationStopped) {
        httpClientInstance.close()
        log.info("HTTP client closed")
    }

    log.info("HTTP client configured")
}
