package htmldownloader.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import event.event.Event;
import event.event.EventPayload;
import event.event.EventType;
import event.event.payload.UrlDiscoveryCreatePayload;
import htmldownloader.entity.DownloadQueue;
import htmldownloader.repository.DownloadQueueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"fru1tworld-webcrawling-url-discovery-v1"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext
public class HtmlDownloaderIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private DownloadQueueRepository downloadQueueRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldProcessUrlDiscoveryEventAndSaveToDatabase() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        String crawlingId = UUID.randomUUID().toString();
        UrlDiscoveryCreatePayload payload = UrlDiscoveryCreatePayload.create(
                crawlingId,
                0,
                2,
                "https://httpbin.org/html",
                "https://httpbin.org/html"
        );

        Event<EventPayload> event = Event.of(eventId, EventType.URL_DISCOVERY_CREATE, payload);
        String message = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(EventType.Topic.URL_DISCOVERY_CREATE, message);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            DownloadQueue savedQueue = downloadQueueRepository.findById(eventId).orElse(null);
            assertThat(savedQueue).isNotNull();
            assertThat(savedQueue.getOriginalUrl()).isEqualTo("https://httpbin.org/html");
            assertThat(savedQueue.getDepth()).isEqualTo(0);
            assertThat(savedQueue.getBfsPath()).isEqualTo("https://httpbin.org/html");
            assertThat(savedQueue.getStatus()).isIn("PROCESSING", "COMPLETED", "FAILED");
        });
    }

    @Test
    void shouldHandleInvalidUrlGracefully() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        String crawlingId = UUID.randomUUID().toString();
        UrlDiscoveryCreatePayload payload = UrlDiscoveryCreatePayload.create(
                crawlingId,
                0,
                1,
                "https://invalid-url-that-does-not-exist.example",
                "https://invalid-url-that-does-not-exist.example"
        );

        Event<EventPayload> event = Event.of(eventId, EventType.URL_DISCOVERY_CREATE, payload);
        String message = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(EventType.Topic.URL_DISCOVERY_CREATE, message);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            DownloadQueue savedQueue = downloadQueueRepository.findById(eventId).orElse(null);
            assertThat(savedQueue).isNotNull();
            assertThat(savedQueue.getStatus()).isEqualTo("FAILED");
        });
    }

    @Test
    void shouldNotProcessBeyondMaxDepth() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        String crawlingId = UUID.randomUUID().toString();
        UrlDiscoveryCreatePayload payload = UrlDiscoveryCreatePayload.create(
                crawlingId,
                5,
                2,  // maxDepth is 2, but current depth is 5
                "https://httpbin.org/html",
                "https://httpbin.org/html"
        );

        Event<EventPayload> event = Event.of(eventId, EventType.URL_DISCOVERY_CREATE, payload);
        String message = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(EventType.Topic.URL_DISCOVERY_CREATE, message);

        // Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            DownloadQueue savedQueue = downloadQueueRepository.findById(eventId).orElse(null);
            assertThat(savedQueue).isNotNull();
            assertThat(savedQueue.getStatus()).isEqualTo("COMPLETED");
        });
    }
}