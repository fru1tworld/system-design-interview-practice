package url.discovery.producer;

import com.fasterxml.uuid.Generators;
import url.discovery.request.UrlDiscoveryRequestDto;
import event.event.Event;
import event.event.EventPayload;
import event.event.EventType;
import event.event.payload.UrlDiscoveryCreatePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlDiscoveryEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AtomicLong eventIdGenerator = new AtomicLong(1);

    public String publishUrlDiscoveryCreate(UrlDiscoveryRequestDto requestDto) {
        try {
            String crawlingId = Generators.timeBasedGenerator().generate().toString();

            UrlDiscoveryCreatePayload payload = UrlDiscoveryCreatePayload.create(
                    crawlingId,
                    0,
                    requestDto.getMaxDepth(),
                    requestDto.getStartUrl()
            );

            Event<EventPayload> event = Event.of(
                    eventIdGenerator.getAndIncrement(),
                    EventType.URL_DISCOVERY_CREATE,
                    payload
            );

            kafkaTemplate.send(EventType.URL_DISCOVERY_CREATE.getTopic(), event.toJson());
            log.info("[UrlDiscoveryEventProducer.publishUrlDiscoveryCreate] crawlingId={}, url={}", crawlingId, requestDto.getStartUrl());

            return crawlingId;
        } catch (Exception e) {
            log.error("[UrlDiscoveryEventProducer.publishUrlDiscoveryCreate] url={}", requestDto.getStartUrl(), e);
            throw new RuntimeException("Event publishing failed", e);
        }
    }
}