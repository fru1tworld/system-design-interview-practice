package htmldownloader.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import event.event.Event;
import event.event.EventPayload;
import event.event.EventType;
import event.event.payload.UrlDiscoveryCreatePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlDiscoveryProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void produceUrlDiscoveryEvents(String crawlingId, Integer currentDepth, Integer maxDepth,
                                        String currentBfsPath, List<String> discoveredUrls) {
        if (currentDepth >= maxDepth) {
            log.info("[UrlDiscoveryProducer.produceUrlDiscoveryEvents] Max depth reached, skipping URL production: depth={}, maxDepth={}",
                    currentDepth, maxDepth);
            return;
        }

        Integer nextDepth = currentDepth + 1;

        for (String url : discoveredUrls) {
            try {
                // BFS path 업데이트 (| 구분자 사용)
                String newBfsPath = currentBfsPath + "|" + url;

                // UrlDiscoveryCreatePayload 생성
                UrlDiscoveryCreatePayload payload = UrlDiscoveryCreatePayload.create(
                    crawlingId,
                    nextDepth,
                    maxDepth,
                    url,
                    newBfsPath
                );

                // Event 생성 with UUIDv7
                String eventId = UUID.randomUUID().toString(); // TODO: UUIDv7 구현 필요
                Event<EventPayload> event = Event.of(eventId, EventType.URL_DISCOVERY_CREATE, payload);

                // Kafka로 전송
                String message = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(EventType.Topic.URL_DISCOVERY_CREATE, message);

                log.info("[UrlDiscoveryProducer.produceUrlDiscoveryEvents] Sent URL discovery event: eventId={}, url={}, depth={}",
                        eventId, url, nextDepth);

            } catch (Exception e) {
                log.error("[UrlDiscoveryProducer.produceUrlDiscoveryEvents] Failed to produce event for URL: {}", url, e);
            }
        }
    }

    public void produceUrlDiscoveryEvent(String crawlingId, Integer currentDepth, Integer maxDepth,
                                       String currentBfsPath, String discoveredUrl) {
        produceUrlDiscoveryEvents(crawlingId, currentDepth, maxDepth, currentBfsPath, List.of(discoveredUrl));
    }
}