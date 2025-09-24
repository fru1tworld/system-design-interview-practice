package webcrawler.urldiscovery.producer;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.UUIDGenerator;
import webcrawler.urldiscovery.request.UrlDiscoveryRequestDto;
import event.event.Event;
import event.event.EventPayload;
import event.event.EventType;
import event.event.payload.UrlDiscoveryCreatePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import webcrawler.urldiscovery.response.UrlDiscoveryResponseDto;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlDiscoveryEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public UrlDiscoveryResponseDto publishUrlDiscoveryCreate(UrlDiscoveryRequestDto requestDto) {
        try {

            String crawlingId = Generators.timeBasedGenerator().generate().toString();

            UrlDiscoveryCreatePayload payload = UrlDiscoveryCreatePayload.create(
                    crawlingId,
                    0,
                    requestDto.getMaxDepth(),
                    requestDto.getStartUrl(),
                    requestDto.getStartUrl()
            );

            Event<EventPayload> event = Event.of(
                    crawlingId,
                    EventType.URL_DISCOVERY_CREATE,
                    payload
            );

            kafkaTemplate.send(EventType.URL_DISCOVERY_CREATE.getTopic(), event.toJson());
            log.info("[UrlDiscoveryEventProducer.publishUrlDiscoveryCreate] url={}",  requestDto.getStartUrl());

            return UrlDiscoveryResponseDto.create(event.getEventId(), requestDto.getStartUrl(), requestDto.getMaxDepth());
        } catch (Exception e) {
            log.error("[UrlDiscoveryEventProducer.publishUrlDiscoveryCreate] url={}", requestDto.getStartUrl(), e);
            throw new RuntimeException("Event publishing failed", e);
        }
    }
}