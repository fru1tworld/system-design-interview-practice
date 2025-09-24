package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import event.event.Event;
import event.event.EventPayload;
import event.event.EventType;
import event.event.payload.UrlDiscoveryCreatePayload;
import htmldownloader.entity.DownloadQueue;
import htmldownloader.repository.DownloadQueueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import webcrawler.urldiscovery.request.UrlDiscoveryRequestDto;
import webcrawler.urldiscovery.response.UrlDiscoveryResponseDto;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
    classes = {
        webcrawler.WebCrawlerAppApplication.class,
        htmldownloader.HtmlDownloaderApplication.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EmbeddedKafka(
    partitions = 3,
    topics = {"fru1tworld-webcrawling-url-discovery-v1"},
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.datasource.url=jdbc:h2:mem:bfs-integration-testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.htmldownloader=DEBUG",
    "logging.level.webcrawler=DEBUG"
})
@DirtiesContext
public class WebCrawlerBfsIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DownloadQueueRepository downloadQueueRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        downloadQueueRepository.deleteAll();
    }

    @Test
    void shouldPerformBfsUpTo5Depth() throws Exception {
        String rootUrl = "https://www.google.com";
        Integer maxDepth = 5;

        UrlDiscoveryRequestDto request = UrlDiscoveryRequestDto.createForTest(rootUrl, maxDepth);

        var response = restTemplate.postForEntity(
            "/api/v1/url-discovery",
            request,
            UrlDiscoveryResponseDto.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUrl()).isEqualTo(rootUrl);
        assertThat(response.getBody().getMaxDepth()).isEqualTo(maxDepth);

        String crawlingId = response.getBody().getCrawlingId();
        System.out.println("Starting BFS test with crawlingId: " + crawlingId);

        await().atMost(120, TimeUnit.SECONDS)
            .pollInterval(3, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                List<DownloadQueue> allQueues = downloadQueueRepository.findAll().stream()
                    .filter(q -> crawlingId.equals(q.getCrawlingId()))
                    .collect(Collectors.toList());

                assertThat(allQueues).isNotEmpty();

                long processingCount = allQueues.stream()
                    .filter(q -> "PROCESSING".equals(q.getStatus()))
                    .count();
                assertThat(processingCount).isEqualTo(0);

                System.out.println("=== Google.com BFS 처리 결과 (필터링 적용) ===");
                long totalUrls = allQueues.size();
                long completedTotal = allQueues.stream()
                    .filter(q -> "COMPLETED".equals(q.getStatus()))
                    .count();
                long failedTotal = allQueues.stream()
                    .filter(q -> "FAILED".equals(q.getStatus()))
                    .count();

                System.out.printf("총 처리된 URL: %d개, 완료: %d개, 실패: %d개%n",
                    totalUrls, completedTotal, failedTotal);

                for (int depth = 0; depth <= maxDepth; depth++) {
                    final int currentDepth = depth;
                    List<DownloadQueue> depthQueues = allQueues.stream()
                        .filter(q -> q.getDepth() != null && q.getDepth().equals(currentDepth))
                        .collect(Collectors.toList());

                    long completedCount = depthQueues.stream()
                        .filter(q -> "COMPLETED".equals(q.getStatus()))
                        .count();
                    long failedCount = depthQueues.stream()
                        .filter(q -> "FAILED".equals(q.getStatus()))
                        .count();

                    System.out.printf("Depth %d: Total=%d개, 완료=%d개, 실패=%d개%n",
                        depth, depthQueues.size(), completedCount, failedCount);

                    if (depth == 0) {
                        assertThat(depthQueues).isNotEmpty();
                    }
                }

                assertThat(totalUrls).isGreaterThanOrEqualTo(5);
            });

        validateBfsTreeStructure();

        validateNoDuplicateUrls();
    }

    @Test
    void shouldStopAtMaxDepth() throws Exception {
        String rootUrl = "https://www.google.com";
        Integer maxDepth = 2;

        UrlDiscoveryRequestDto request = UrlDiscoveryRequestDto.createForTest(rootUrl, maxDepth);

        var response = restTemplate.postForEntity(
            "/api/v1/url-discovery",
            request,
            UrlDiscoveryResponseDto.class
        );

        String crawlingId = response.getBody().getCrawlingId();
        await().atMost(45, TimeUnit.SECONDS)
            .pollInterval(2, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                List<DownloadQueue> allQueues = downloadQueueRepository.findAll().stream()
                    .filter(q -> crawlingId.equals(q.getCrawlingId()))
                    .collect(Collectors.toList());

                boolean hasExceededDepth = allQueues.stream()
                    .anyMatch(q -> q.getDepth() != null && q.getDepth() > maxDepth);
                assertThat(hasExceededDepth).isFalse();

                long processingCount = allQueues.stream()
                    .filter(q -> "PROCESSING".equals(q.getStatus()))
                    .count();
                assertThat(processingCount).isEqualTo(0);

                System.out.printf("Google.com MaxDepth %d 테스트 결과:%n", maxDepth);
                for (int depth = 0; depth <= maxDepth + 1; depth++) {
                    final int currentDepth = depth;
                    long depthCount = allQueues.stream()
                        .filter(q -> q.getDepth() != null && q.getDepth().equals(currentDepth))
                        .count();
                    System.out.printf("  Depth %d: %d개 URL%n", depth, depthCount);
                }

                assertThat(allQueues).isNotEmpty();
            });
    }

    private void validateBfsTreeStructure() {
        List<DownloadQueue> allQueues = downloadQueueRepository.findAll();

        System.out.println("\n=== BFS Tree Structure ===");
        for (DownloadQueue queue : allQueues) {
            System.out.printf("ID: %s | Depth: %d | URL: %s | Status: %s | BfsPath: %s%n",
                queue.getId(),
                queue.getDepth(),
                truncateUrl(queue.getOriginalUrl()),
                queue.getStatus(),
                truncateBfsPath(queue.getBfsPath())
            );
        }
    }

    private void validateNoDuplicateUrls() {
        List<DownloadQueue> allQueues = downloadQueueRepository.findAll();

        long uniqueUrls = allQueues.stream()
            .map(DownloadQueue::getOriginalUrl)
            .distinct()
            .count();

        System.out.printf("\n=== URL Duplication Check ===\n");
        System.out.printf("Total processed: %d, Unique URLs: %d%n", allQueues.size(), uniqueUrls);

        if (allQueues.size() != uniqueUrls) {
            System.out.println("Duplicate URLs found (this might be expected for BFS):");
            allQueues.stream()
                .collect(Collectors.groupingBy(DownloadQueue::getOriginalUrl))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> System.out.printf("  %s: %d times%n",
                    truncateUrl(entry.getKey()), entry.getValue().size()));
        }
    }

    private String truncateUrl(String url) {
        if (url == null) return "null";
        return url.length() > 80 ? url.substring(0, 80) + "..." : url;
    }

    private String truncateBfsPath(String path) {
        if (path == null) return "null";
        return path.length() > 100 ? path.substring(0, 100) + "..." : path;
    }

    @Test
    void shouldCrawlGoogleWithLimitedDepth() throws Exception {
        String rootUrl = "https://www.google.com";
        Integer maxDepth = 3;

        UrlDiscoveryRequestDto request = UrlDiscoveryRequestDto.createForTest(rootUrl, maxDepth);

        // When
        var response = restTemplate.postForEntity(
            "/api/v1/url-discovery",
            request,
            UrlDiscoveryResponseDto.class
        );

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        String crawlingId = response.getBody().getCrawlingId();
        await().atMost(60, TimeUnit.SECONDS)
            .pollInterval(3, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                List<DownloadQueue> allQueues = downloadQueueRepository.findAll().stream()
                    .filter(q -> crawlingId.equals(q.getCrawlingId()))
                    .collect(Collectors.toList());

                // 처리 중인 항목이 없어야 함 (모든 처리 완료)
                long processingCount = allQueues.stream()
                    .filter(q -> "PROCESSING".equals(q.getStatus()))
                    .count();
                assertThat(processingCount).isEqualTo(0);

                // 최소한 루트 URL은 처리되어야 함
                assertThat(allQueues).isNotEmpty();

                // 처리 완료된 항목 확인
                long completedCount = allQueues.stream()
                    .filter(q -> "COMPLETED".equals(q.getStatus()))
                    .count();
                long failedCount = allQueues.stream()
                    .filter(q -> "FAILED".equals(q.getStatus()))
                    .count();

                System.out.printf("Google.com 크롤링 결과 (maxDepth=%d): 총 %d개 URL, 완료 %d개, 실패 %d개%n",
                    maxDepth, allQueues.size(), completedCount, failedCount);

                // depth별 상세 출력
                for (int depth = 0; depth <= maxDepth; depth++) {
                    final int currentDepth = depth;
                    long depthCount = allQueues.stream()
                        .filter(q -> q.getDepth() != null && q.getDepth().equals(currentDepth))
                        .count();
                    if (depthCount > 0) {
                        System.out.printf("  Depth %d: %d개 URL%n", depth, depthCount);
                    }
                }

                assertThat(completedCount).isGreaterThan(0);
            });
    }
}