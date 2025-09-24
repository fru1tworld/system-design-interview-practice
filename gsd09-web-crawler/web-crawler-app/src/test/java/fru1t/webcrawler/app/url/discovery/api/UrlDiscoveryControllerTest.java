package fru1t.webcrawler.app.url.discovery.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import webcrawler.urldiscovery.request.UrlDiscoveryRequestDto;
import webcrawler.urldiscovery.request.UrlDiscoveryBatchResponseDto;
import webcrawler.urldiscovery.response.UrlDiscoveryResponseDto;
import event.event.Event;
import event.event.EventPayload;
import event.event.payload.UrlDiscoveryCreatePayload;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = webcrawler.WebCrawlerAppApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1,
    topics = {"fru1tworld-webcrawling-url-discovery-v1"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@DisplayName("URL 검색 API 테스트")
class UrlDiscoveryControllerTest {
    private static final String TOPIC = "fru1tworld-webcrawling-url-discovery-v1";
    private static final String TEST_URL = "https://api-test.com";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private KafkaMessageListenerContainer<String, String> container;
    private BlockingQueue<ConsumerRecord<String, String>> records;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString(),
            ConsumerConfig.GROUP_ID_CONFIG, "api-test-group",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );

        DefaultKafkaConsumerFactory<String, String> consumerFactory =
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();

        // 컨테이너 시작 대기
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("단건 URL 검색 API가 정상적으로 동작해야 한다")
    void createUrlDiscovery_shouldReturnResponseAndSendKafkaMessage() throws InterruptedException {
        // Given
        UrlDiscoveryRequestDto requestDto = UrlDiscoveryRequestDto.createForTest(TEST_URL, 3);

        // When
        ResponseEntity<UrlDiscoveryResponseDto> response = restTemplate.postForEntity(
            "/api/v1/url-discovery",
            requestDto,
            UrlDiscoveryResponseDto.class
        );

        // Then - HTTP 응답 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUrl()).isEqualTo(TEST_URL);
        assertThat(response.getBody().getMaxDepth()).isEqualTo(3);
        assertThat(response.getBody().getCrawlingId()).isNotEmpty();

        // Then - Kafka 메시지 검증
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();

        String messageJson = received.value();
        Event<EventPayload> event = Event.fromJson(messageJson);
        UrlDiscoveryCreatePayload payload = (UrlDiscoveryCreatePayload) event.getPayload();

        assertThat(payload.getCrawlingId()).isEqualTo(response.getBody().getCrawlingId());
        assertThat(payload.getUrl()).isEqualTo(TEST_URL);
        assertThat(payload.getMaxDepth()).isEqualTo(3);

        container.stop();
    }

    @Test
    @DisplayName("maxDepth가 null인 단건 요청 시 기본값 10이 적용되어야 한다")
    void createUrlDiscovery_shouldUseDefaultMaxDepthWhenNull() throws InterruptedException {
        // Given
        UrlDiscoveryRequestDto requestDto = UrlDiscoveryRequestDto.createForTest(TEST_URL, null);

        // When
        ResponseEntity<UrlDiscoveryResponseDto> response = restTemplate.postForEntity(
            "/api/v1/url-discovery",
            requestDto,
            UrlDiscoveryResponseDto.class
        );

        // Then - HTTP 응답 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMaxDepth()).isEqualTo(10);

        // Then - Kafka 메시지 검증
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();

        Event<EventPayload> event = Event.fromJson(received.value());
        UrlDiscoveryCreatePayload payload = (UrlDiscoveryCreatePayload) event.getPayload();

        assertThat(payload.getMaxDepth()).isEqualTo(10);

        container.stop();
    }

    @Test
    @DisplayName("복수 URL 검색 API가 정상적으로 동작해야 한다")
    void createUrlDiscoveries_shouldReturnBatchResponseAndSendKafkaMessages() throws InterruptedException {
        // Given
        List<UrlDiscoveryRequestDto> requestDtos = Arrays.asList(
            UrlDiscoveryRequestDto.createForTest("https://example1.com", 2),
            UrlDiscoveryRequestDto.createForTest("https://example2.com", 4)
        );

        // When
        ResponseEntity<UrlDiscoveryBatchResponseDto> response = restTemplate.postForEntity(
            "/api/v1/url-discovery/batch",
            requestDtos,
            UrlDiscoveryBatchResponseDto.class
        );

        // Then - HTTP 응답 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccessResults()).hasSize(2);
        assertThat(response.getBody().getErrors()).isEmpty();

        // 첫 번째 결과 검증
        UrlDiscoveryResponseDto firstResult = response.getBody().getSuccessResults().get(0);
        assertThat(firstResult.getUrl()).isEqualTo("https://example1.com");
        assertThat(firstResult.getMaxDepth()).isEqualTo(2);

        // 두 번째 결과 검증
        UrlDiscoveryResponseDto secondResult = response.getBody().getSuccessResults().get(1);
        assertThat(secondResult.getUrl()).isEqualTo("https://example2.com");
        assertThat(secondResult.getMaxDepth()).isEqualTo(4);

        // Then - Kafka 메시지 2개 검증
        ConsumerRecord<String, String> firstMessage = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, String> secondMessage = records.poll(10, TimeUnit.SECONDS);

        assertThat(firstMessage).isNotNull();
        assertThat(secondMessage).isNotNull();

        container.stop();
    }
}