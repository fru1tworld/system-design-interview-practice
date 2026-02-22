package htmldownloader.kafka

import event.Event
import event.UrlDiscoveryCreatePayload
import htmldownloader.domain.downloadqueue.*
import htmldownloader.plugins.database
import htmldownloader.plugins.httpClient
import htmldownloader.plugins.kafkaConsumer
import htmldownloader.plugins.kafkaProducer
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.time.Duration

class UrlDiscoveryConsumer(
    private val application: Application
) {
    private val log = LoggerFactory.getLogger(UrlDiscoveryConsumer::class.java)

    private val repository by lazy { DownloadQueueRepository(application.database) }
    private val htmlDownloadService by lazy { HtmlDownloadService(application.httpClient) }
    private val urlDiscoveryProducer by lazy { UrlDiscoveryProducer(application.kafkaProducer) }
    private val bfsFilterService by lazy { BfsFilterService(repository) }
    private val downloadQueueService by lazy {
        DownloadQueueService(repository, htmlDownloadService, urlDiscoveryProducer, bfsFilterService)
    }

    suspend fun startConsuming() {
        log.info("[UrlDiscoveryConsumer] Starting Kafka consumer...")

        withContext(Dispatchers.IO) {
            while (isActive) {
                try {
                    val records = application.kafkaConsumer.poll(Duration.ofMillis(100))

                    for (record in records) {
                        try {
                            processMessage(record.value())
                        } catch (e: Exception) {
                            log.error("[UrlDiscoveryConsumer] Error processing record: {}", record.value(), e)
                        }
                    }
                } catch (e: Exception) {
                    if (isActive) {
                        log.error("[UrlDiscoveryConsumer] Error polling Kafka: {}", e.message, e)
                    }
                }
            }
        }
    }

    private suspend fun processMessage(message: String) {
        try {
            val event = Event.fromJsonDynamic(message)
            if (event == null) {
                log.warn("[UrlDiscoveryConsumer] Failed to parse event from message: {}", message)
                return
            }

            val payload = event.payload as? UrlDiscoveryCreatePayload
            if (payload == null) {
                log.warn("[UrlDiscoveryConsumer] Payload is not UrlDiscoveryCreatePayload: {}", message)
                return
            }

            log.info("[UrlDiscoveryConsumer] Received URL discovery event: eventId={}, url={}, depth={}",
                event.eventId, payload.url, payload.depth)

            downloadQueueService.consumeUrlDiscovery(event.eventId, payload)

        } catch (e: Exception) {
            log.error("[UrlDiscoveryConsumer] Unexpected error processing message: {}", message, e)
        }
    }
}
