package htmldownloader.consumer;

import event.event.Event;
import event.event.EventPayload;
import event.event.payload.UrlDiscoveryCreatePayload;
import htmldownloader.service.DownloadQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlDiscoveryConsumer {

    private final DownloadQueueService downloadQueueService;

    @KafkaListener(topics = "#{T(event.event.EventType$Topic).URL_DISCOVERY_CREATE}")
    public void consumeUrlDiscovery(String message) {
        try {
            Event<EventPayload> event = Event.fromJson(message);
            if (event == null) {
                log.warn("[UrlDiscoveryConsumer.consumeUrlDiscovery] Failed to parse event from message: {}", message);
                return;
            }

            UrlDiscoveryCreatePayload payload = (UrlDiscoveryCreatePayload) event.getPayload();
            log.info("[UrlDiscoveryConsumer.consumeUrlDiscovery] Received URL discovery event: eventId={}, url={}, depth={}",
                    event.getEventId(), payload.getUrl(), payload.getDepth());

            downloadQueueService.consumeUrlDiscovery(event.getEventId(), payload);

        } catch (Exception e) {
            log.error("[UrlDiscoveryConsumer.consumeUrlDiscovery] Unexpected error processing message: {}", message, e);
        }
    }
}