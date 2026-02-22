package integration

import event.Event
import event.EventType
import event.UrlDiscoveryCreatePayload
import htmldownloader.domain.downloadqueue.DownloadQueueRepository
import htmldownloader.domain.downloadqueue.DownloadQueues
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import webcrawler.domain.urldiscovery.UrlDiscoveryRequestDto
import webcrawler.domain.urldiscovery.UrlDiscoveryResponseDto
import java.time.Duration
import java.util.*
import kotlin.test.assertTrue

/**
 * Web Crawler BFS Integration Test
 *
 * 이 테스트는 Kafka와 실제 네트워크 환경이 필요합니다.
 * 로컬에서 실행하려면 Docker Compose로 Kafka를 먼저 시작하세요.
 */
class WebCrawlerBfsIntegrationTest {

    private lateinit var database: Database
    private lateinit var repository: DownloadQueueRepository

    @BeforeEach
    fun setup() {
        // H2 In-Memory Database 설정
        database = Database.connect(
            url = "jdbc:h2:mem:test_${System.currentTimeMillis()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(DownloadQueues)
        }

        repository = DownloadQueueRepository(database)
    }

    @Test
    fun `should create url discovery request`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }

        // URL Discovery 요청 DTO 테스트
        val request = UrlDiscoveryRequestDto(
            startUrl = "https://example.com",
            maxDepth = 5
        )

        request.startUrl shouldBe "https://example.com"
        request.getEffectiveMaxDepth() shouldBe 5
    }

    @Test
    fun `should use default max depth when not specified`() {
        val request = UrlDiscoveryRequestDto(
            startUrl = "https://example.com",
            maxDepth = null
        )

        request.getEffectiveMaxDepth() shouldBe 10 // DEFAULT_MAX_DEPTH
    }

    @Test
    fun `should serialize and deserialize event correctly`() {
        val payload = UrlDiscoveryCreatePayload.create(
            crawlingId = "test-crawling-id",
            depth = 0,
            maxDepth = 5,
            url = "https://example.com",
            bfsPath = "https://example.com"
        )

        val event = Event.of(
            eventId = "test-event-id",
            type = EventType.URL_DISCOVERY_CREATE,
            payload = payload
        )

        val json = event.toJson()
        json shouldNotBe null

        val deserializedEvent = Event.fromJson<UrlDiscoveryCreatePayload>(json)
        deserializedEvent shouldNotBe null
        deserializedEvent?.eventId shouldBe "test-event-id"
        deserializedEvent?.payload?.crawlingId shouldBe "test-crawling-id"
    }

    @Test
    fun `should save download queue to database`() {
        val downloadQueue = htmldownloader.domain.downloadqueue.DownloadQueue.create(
            id = "test-id",
            crawlingId = "test-crawling-id",
            originalUrl = "https://example.com",
            bfsPath = "https://example.com",
            depth = 0,
            status = "PENDING"
        )

        repository.save(downloadQueue)

        val found = repository.findById("test-id")
        found shouldNotBe null
        found?.originalUrl shouldBe "https://example.com"
        found?.status shouldBe "PENDING"
    }

    @Test
    fun `should check bfs path existence`() {
        val downloadQueue = htmldownloader.domain.downloadqueue.DownloadQueue.create(
            id = "test-id",
            crawlingId = "test-crawling-id",
            originalUrl = "https://example.com",
            bfsPath = "https://example.com|https://example.com/page1",
            depth = 1,
            status = "COMPLETED"
        )

        repository.save(downloadQueue)

        repository.existsByBfsPath("https://example.com|https://example.com/page1") shouldBe true
        repository.existsByBfsPath("https://example.com|https://example.com/page2") shouldBe false
    }

    @Test
    fun `should update download queue status`() {
        val downloadQueue = htmldownloader.domain.downloadqueue.DownloadQueue.create(
            id = "test-id",
            crawlingId = "test-crawling-id",
            originalUrl = "https://example.com",
            bfsPath = "https://example.com",
            depth = 0,
            status = "PENDING"
        )

        repository.save(downloadQueue)
        repository.updateStatus("test-id", "COMPLETED")

        val found = repository.findById("test-id")
        found?.status shouldBe "COMPLETED"
    }

    @Test
    @Disabled("Requires running Kafka instance")
    fun `should produce and consume kafka messages`() {
        val bootstrapServers = "localhost:9092"
        val topic = EventType.Topic.URL_DISCOVERY_CREATE

        // Producer 설정
        val producerProps = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        }
        val producer = KafkaProducer<String, String>(producerProps)

        // Consumer 설정
        val consumerProps = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-${System.currentTimeMillis()}")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }
        val consumer = KafkaConsumer<String, String>(consumerProps)
        consumer.subscribe(listOf(topic))

        // 이벤트 생성 및 전송
        val payload = UrlDiscoveryCreatePayload.create(
            crawlingId = "test-crawling-id",
            depth = 0,
            maxDepth = 5,
            url = "https://example.com",
            bfsPath = "https://example.com"
        )

        val event = Event.of(
            eventId = "test-event-id",
            type = EventType.URL_DISCOVERY_CREATE,
            payload = payload
        )

        producer.send(ProducerRecord(topic, "test-key", event.toJson())).get()

        // 메시지 수신 확인
        val records = consumer.poll(Duration.ofSeconds(10))
        assertTrue(records.count() > 0, "Should receive at least one message")

        producer.close()
        consumer.close()
    }
}
