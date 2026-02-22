package htmldownloader

import htmldownloader.kafka.UrlDiscoveryConsumer
import htmldownloader.plugins.configureDatabase
import htmldownloader.plugins.configureHttpClient
import htmldownloader.plugins.configureKafka
import htmldownloader.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.launch

fun main() {
    embeddedServer(CIO, port = 10002, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureDatabase()
    configureHttpClient()
    configureKafka()

    // Start Kafka consumer in background
    val consumer = UrlDiscoveryConsumer(this)
    launch {
        consumer.startConsuming()
    }

    log.info("HTML Downloader application started on port 10002")
}
