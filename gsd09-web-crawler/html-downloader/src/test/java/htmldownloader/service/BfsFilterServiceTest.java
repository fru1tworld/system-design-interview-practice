package htmldownloader.service;

import htmldownloader.repository.DownloadQueueRepository;
import htmldownloader.utils.DateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BfsFilterServiceTest {

    @Mock
    private DownloadQueueRepository downloadQueueRepository;

    @InjectMocks
    private BfsFilterService bfsFilterService;

    @Test
    @DisplayName("rootDomain이 다른 URL은 필터링되어야 한다")
    void shouldFilterUrlsWithDifferentRootDomain() {
        // Given
        String crawlingId = "test-crawling-id";
        String currentBfsPath = "https://google.com";
        String rootDomain = "google.com";

        List<String> extractedUrls = Arrays.asList(
                "https://google.com/search",           // 같은 도메인 - 허용
                "https://www.google.com/images",       // 같은 도메인 - 허용
                "https://facebook.com/page",           // 다른 도메인 - 필터링
                "https://google.com/maps"              // 같은 도메인 - 허용
        );

        when(downloadQueueRepository.findRootDomainByCrawlingId(crawlingId))
                .thenReturn(rootDomain);
        when(downloadQueueRepository.existsByBfsPath(anyString()))
                .thenReturn(false);
        when(downloadQueueRepository.existsByCrawlingIdAndBfsPath(anyString(), anyString()))
                .thenReturn(false);
        when(downloadQueueRepository.existsByOriginalUrlWithinOneMonth(anyString(), any(LocalDateTime.class)))
                .thenReturn(false);

        // When
        List<String> filteredUrls = bfsFilterService.filterUrlsForBfs(crawlingId, currentBfsPath, extractedUrls);

        // Then
        assertThat(filteredUrls).hasSize(3);
        assertThat(filteredUrls).containsExactlyInAnyOrder(
                "https://google.com/search",
                "https://www.google.com/images",
                "https://google.com/maps"
        );
        assertThat(filteredUrls).doesNotContain("https://facebook.com/page");
    }

    @Test
    @DisplayName("중복된 bfsPath URL은 필터링되어야 한다")
    void shouldFilterUrlsWithDuplicateBfsPath() {
        // Given
        String crawlingId = "test-crawling-id";
        String currentBfsPath = "https://google.com";
        String rootDomain = "google.com";

        List<String> extractedUrls = Arrays.asList(
                "https://google.com/search",
                "https://google.com/images"
        );

        when(downloadQueueRepository.findRootDomainByCrawlingId(crawlingId))
                .thenReturn(rootDomain);
        when(downloadQueueRepository.existsByBfsPath("https://google.com|https://google.com/search"))
                .thenReturn(true);  // 중복된 경로
        when(downloadQueueRepository.existsByBfsPath("https://google.com|https://google.com/images"))
                .thenReturn(false); // 새로운 경로
        when(downloadQueueRepository.existsByCrawlingIdAndBfsPath(anyString(), anyString()))
                .thenReturn(false);
        when(downloadQueueRepository.existsByOriginalUrlWithinOneMonth(anyString(), any(LocalDateTime.class)))
                .thenReturn(false);

        // When
        List<String> filteredUrls = bfsFilterService.filterUrlsForBfs(crawlingId, currentBfsPath, extractedUrls);

        // Then
        assertThat(filteredUrls).hasSize(1);
        assertThat(filteredUrls).containsExactly("https://google.com/images");
    }

    @Test
    @DisplayName("1달 이내 방문한 URL은 필터링되어야 한다")
    void shouldFilterUrlsVisitedWithinOneMonth() {
        // Given
        String crawlingId = "test-crawling-id";
        String currentBfsPath = "https://google.com";
        String rootDomain = "google.com";

        List<String> extractedUrls = Arrays.asList(
                "https://google.com/search",
                "https://google.com/images"
        );

        when(downloadQueueRepository.findRootDomainByCrawlingId(crawlingId))
                .thenReturn(rootDomain);
        when(downloadQueueRepository.existsByBfsPath(anyString()))
                .thenReturn(false);
        when(downloadQueueRepository.existsByCrawlingIdAndBfsPath(anyString(), anyString()))
                .thenReturn(false);
        when(downloadQueueRepository.existsByOriginalUrlWithinOneMonth(eq("https://google.com/search"), any(LocalDateTime.class)))
                .thenReturn(true);  // 1달 이내 방문
        when(downloadQueueRepository.existsByOriginalUrlWithinOneMonth(eq("https://google.com/images"), any(LocalDateTime.class)))
                .thenReturn(false); // 1달 이내 미방문

        // When
        List<String> filteredUrls = bfsFilterService.filterUrlsForBfs(crawlingId, currentBfsPath, extractedUrls);

        // Then
        assertThat(filteredUrls).hasSize(1);
        assertThat(filteredUrls).containsExactly("https://google.com/images");
    }

    @Test
    @DisplayName("모든 필터링 조건에 걸리면 빈 리스트가 반환되어야 한다")
    void shouldReturnEmptyListWhenAllUrlsAreFiltered() {
        // Given
        String crawlingId = "test-crawling-id";
        String currentBfsPath = "https://google.com";
        String rootDomain = "google.com";

        List<String> extractedUrls = Arrays.asList(
                "https://facebook.com/page",           // 다른 도메인
                "https://google.com/search"            // 1달 이내 방문
        );

        when(downloadQueueRepository.findRootDomainByCrawlingId(crawlingId))
                .thenReturn(rootDomain);
        when(downloadQueueRepository.existsByOriginalUrlWithinOneMonth(eq("https://google.com/search"), any(LocalDateTime.class)))
                .thenReturn(true);

        // When
        List<String> filteredUrls = bfsFilterService.filterUrlsForBfs(crawlingId, currentBfsPath, extractedUrls);

        // Then
        assertThat(filteredUrls).isEmpty();
    }
}