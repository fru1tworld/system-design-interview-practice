package fru1t.webcrawler.app.url.discovery.producer;

import url.discovery.request.UrlDiscoveryRequestDto;
import event.event.Event;
import event.event.EventPayload;
import event.event.EventType;
import event.event.payload.UrlDiscoveryCreatePayload;
import url.discovery.producer.UrlDiscoveryEventProducer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = webcrawler.WebCrawlerAppApplication.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1,
    topics = {"fru1tworld-webcrawling-url-discovery-v1"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@DisplayName("URL 검색 이벤트 발행 테스트")
class UrlDiscoveryEventProducerTest {
    private static final String TOPIC = "fru1tworld-webcrawling-url-discovery-v1";
    private static final String TEST_URL = "https://example.com";

    @Autowired
    private UrlDiscoveryEventProducer urlDiscoveryEventProducer;

    private KafkaMessageListenerContainer<String, String> container;
    private BlockingQueue<ConsumerRecord<String, String>> records;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString(),
            ConsumerConfig.GROUP_ID_CONFIG, "test-group",
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
    @DisplayName("URL 검색 이벤트가 Kafka에 정상적으로 발행되어야 한다")
    void publishUrlDiscoveryCreate_shouldSendMessageToKafka() throws InterruptedException {
        // Given
        Integer testMaxDepth = 3;
        UrlDiscoveryRequestDto requestDto = UrlDiscoveryRequestDto.createForTest(TEST_URL, testMaxDepth);

        // When
        String crawlingId = urlDiscoveryEventProducer.publishUrlDiscoveryCreate(requestDto);

        // Then
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.topic()).isEqualTo(TOPIC);

        // JSON 메시지 검증
        String messageJson = received.value();
        assertThat(messageJson).contains(crawlingId);
        assertThat(messageJson).contains(TEST_URL);

        // Event 객체로 역직렬화해서 검증
        Event<EventPayload> event = Event.fromJson(messageJson);
        assertThat(event).isNotNull();
        assertThat(event.getType()).isEqualTo(EventType.URL_DISCOVERY_CREATE);

        UrlDiscoveryCreatePayload payload = (UrlDiscoveryCreatePayload) event.getPayload();
        assertThat(payload.getCrawlingId()).isEqualTo(crawlingId);
        assertThat(payload.getUrl()).isEqualTo(TEST_URL);
        assertThat(payload.getMaxDepth()).isEqualTo(testMaxDepth);
        assertThat(payload.getDepth()).isEqualTo(0);

        container.stop();
    }

    @Test
    @DisplayName("maxDepth가 null일 때 기본값 10이 적용되어야 한다")
    void publishUrlDiscoveryCreate_shouldUseDefaultMaxDepthWhenNull() throws InterruptedException {
        // Given
        Integer testMaxDepth = null; // null로 설정
        UrlDiscoveryRequestDto requestDto = UrlDiscoveryRequestDto.createForTest(TEST_URL, testMaxDepth);

        // When
        String crawlingId = urlDiscoveryEventProducer.publishUrlDiscoveryCreate(requestDto);

        // Then
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();

        String messageJson = received.value();
        Event<EventPayload> event = Event.fromJson(messageJson);
        UrlDiscoveryCreatePayload payload = (UrlDiscoveryCreatePayload) event.getPayload();

        // 기본값 10이 적용되었는지 확인
        assertThat(payload.getMaxDepth()).isEqualTo(10);
        assertThat(payload.getCrawlingId()).isEqualTo(crawlingId);
        assertThat(payload.getUrl()).isEqualTo(TEST_URL);
        assertThat(payload.getDepth()).isEqualTo(0);

        container.stop();
    }

    @Test
    @DisplayName("잘못된 URL로 요청 시에도 이벤트가 발행되어야 한다")
    void publishUrlDiscoveryCreate_shouldSendMessageEvenWithInvalidUrl() throws InterruptedException {
        // Given
        String invalidUrl = "invalid-url-format";
        Integer testMaxDepth = 3;
        UrlDiscoveryRequestDto requestDto = UrlDiscoveryRequestDto.createForTest(invalidUrl, testMaxDepth);

        // When
        String crawlingId = urlDiscoveryEventProducer.publishUrlDiscoveryCreate(requestDto);

        // Then
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();

        String messageJson = received.value();
        Event<EventPayload> event = Event.fromJson(messageJson);
        UrlDiscoveryCreatePayload payload = (UrlDiscoveryCreatePayload) event.getPayload();

        assertThat(payload.getCrawlingId()).isEqualTo(crawlingId);
        assertThat(payload.getUrl()).isEqualTo(invalidUrl);
        assertThat(payload.getMaxDepth()).isEqualTo(testMaxDepth);

        container.stop();
    }
}