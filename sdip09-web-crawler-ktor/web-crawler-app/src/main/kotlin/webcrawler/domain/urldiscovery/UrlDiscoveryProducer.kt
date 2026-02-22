package webcrawler.domain.urldiscovery

import com.github.f4b6a3.uuid.UuidCreator
import event.Event
import event.EventType
import event.UrlDiscoveryCreatePayload
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory

class UrlDiscoveryProducer(
    private val kafkaProducer: KafkaProducer<String, String>
) {
    private val log = LoggerFactory.getLogger(UrlDiscoveryProducer::class.java)

    suspend fun publishUrlDiscoveryCreate(requestDto: UrlDiscoveryRequestDto): UrlDiscoveryResponseDto {
        return withContext(Dispatchers.IO) {
            try {
                val crawlingId = UuidCreator.getTimeOrderedEpoch().toString()

                val payload = UrlDiscoveryCreatePayload.create(
                    crawlingId = crawlingId,
                    depth = 0,
                    maxDepth = requestDto.getEffectiveMaxDepth(),
                    url = requestDto.startUrl,
                    bfsPath = requestDto.startUrl
                )

                val event = Event.of(
                    eventId = crawlingId,
                    type = EventType.URL_DISCOVERY_CREATE,
                    payload = payload
                )

                val record = ProducerRecord(
                    EventType.URL_DISCOVERY_CREATE.topic,
                    crawlingId,
                    event.toJson()
                )

                kafkaProducer.send(record).get()
                log.info("[UrlDiscoveryProducer] Published URL discovery event: url={}", requestDto.startUrl)

                UrlDiscoveryResponseDto.create(
                    crawlingId = crawlingId,
                    url = requestDto.startUrl,
                    maxDepth = requestDto.getEffectiveMaxDepth()
                )
            } catch (e: Exception) {
                log.error("[UrlDiscoveryProducer] Failed to publish: url={}", requestDto.startUrl, e)
                throw RuntimeException("Event publishing failed", e)
            }
        }
    }
}
