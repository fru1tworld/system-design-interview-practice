package htmldownloader.kafka

import com.github.f4b6a3.uuid.UuidCreator
import event.Event
import event.EventType
import event.UrlDiscoveryCreatePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory

class UrlDiscoveryProducer(
    private val kafkaProducer: KafkaProducer<String, String>
) {
    private val log = LoggerFactory.getLogger(UrlDiscoveryProducer::class.java)

    suspend fun produceUrlDiscoveryEvents(
        crawlingId: String,
        currentDepth: Int,
        maxDepth: Int,
        currentBfsPath: String,
        discoveredUrls: List<String>
    ) {
        if (currentDepth >= maxDepth) {
            log.info("[UrlDiscoveryProducer] Max depth reached, skipping URL production: depth={}, maxDepth={}",
                currentDepth, maxDepth)
            return
        }

        val nextDepth = currentDepth + 1

        for (url in discoveredUrls) {
            try {
                val newBfsPath = "$currentBfsPath|$url"

                val payload = UrlDiscoveryCreatePayload.create(
                    crawlingId = crawlingId,
                    depth = nextDepth,
                    maxDepth = maxDepth,
                    url = url,
                    bfsPath = newBfsPath
                )

                val eventId = UuidCreator.getTimeOrderedEpoch().toString()
                val event = Event.of(eventId, EventType.URL_DISCOVERY_CREATE, payload)

                withContext(Dispatchers.IO) {
                    val record = ProducerRecord(
                        EventType.Topic.URL_DISCOVERY_CREATE,
                        eventId,
                        event.toJson()
                    )
                    kafkaProducer.send(record).get()
                }

                log.info("[UrlDiscoveryProducer] Sent URL discovery event: eventId={}, url={}, depth={}",
                    eventId, url, nextDepth)

            } catch (e: Exception) {
                log.error("[UrlDiscoveryProducer] Failed to produce event for URL: {}", url, e)
            }
        }
    }

    suspend fun produceUrlDiscoveryEvent(
        crawlingId: String,
        currentDepth: Int,
        maxDepth: Int,
        currentBfsPath: String,
        discoveredUrl: String
    ) {
        produceUrlDiscoveryEvents(crawlingId, currentDepth, maxDepth, currentBfsPath, listOf(discoveredUrl))
    }
}
