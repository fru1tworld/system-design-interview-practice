package htmldownloader.service;

import htmldownloader.repository.DownloadQueueRepository;
import htmldownloader.utils.DateUtils;
import htmldownloader.utils.LinkExtractionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BFS 필터링 서비스
 * - bfsPath 중복 방지
 * - 1달 이내 방문 URL 필터링
 * - rootDomain 필터링
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BfsFilterService {

    private final DownloadQueueRepository downloadQueueRepository;

    /**
     * BFS에서 다음 depth로 탐색할 URL들을 필터링
     * @param crawlingId 크롤링 세션 ID
     * @param currentBfsPath 현재 BFS 경로
     * @param extractedUrls 추출된 URL 목록
     * @return 필터링된 URL 목록
     */
    public List<String> filterUrlsForBfs(String crawlingId, String currentBfsPath, List<String> extractedUrls) {
        // 1. rootDomain 조회
        String rootDomain = downloadQueueRepository.findRootDomainByCrawlingId(crawlingId);
        if (rootDomain == null) {
            log.warn("[BfsFilterService.filterUrlsForBfs] Root domain not found for crawlingId: {}", crawlingId);
            return List.of();
        }

        log.info("[BfsFilterService.filterUrlsForBfs] Filtering {} URLs for crawlingId: {}, rootDomain: {}",
                extractedUrls.size(), crawlingId, rootDomain);

        List<String> filteredUrls = extractedUrls.stream()
                .filter(url -> filterByRootDomain(url, rootDomain))
                .filter(url -> filterByBfsPath(crawlingId, currentBfsPath, url))
                .filter(url -> filterByRecentVisit(url))
                .collect(Collectors.toList());

        log.info("[BfsFilterService.filterUrlsForBfs] Filtered result: {} URLs remaining from {} original URLs",
                filteredUrls.size(), extractedUrls.size());

        return filteredUrls;
    }

    /**
     * rootDomain 필터링: rootDomain과 같은 도메인만 허용
     */
    private boolean filterByRootDomain(String url, String rootDomain) {
        String urlDomain = LinkExtractionUtils.extractRootDomain(url);
        boolean isAllowed = rootDomain.equals(urlDomain);

        if (!isAllowed) {
            log.debug("[BfsFilterService.filterByRootDomain] Filtered out URL (different domain): {} (expected: {})",
                     urlDomain, rootDomain);
        }

        return isAllowed;
    }

    /**
     * bfsPath 중복 필터링: 이미 방문한 경로는 제외
     */
    private boolean filterByBfsPath(String crawlingId, String currentBfsPath, String url) {
        String newBfsPath = currentBfsPath + "|" + url;

        // 전체적으로 중복된 경로 체크
        boolean isDuplicatePath = downloadQueueRepository.existsByBfsPath(newBfsPath);

        // 현재 크롤링 세션에서 중복된 경로 체크 (더 엄격한 필터링)
        boolean isDuplicateInSession = downloadQueueRepository.existsByCrawlingIdAndBfsPath(crawlingId, newBfsPath);

        if (isDuplicatePath || isDuplicateInSession) {
            log.debug("[BfsFilterService.filterByBfsPath] Filtered out URL (duplicate path): {}", newBfsPath);
            return false;
        }

        return true;
    }

    /**
     * 1달 이내 방문 필터링: 1달 이내에 방문한 URL은 제외
     */
    private boolean filterByRecentVisit(String url) {
        boolean isRecentlyVisited = downloadQueueRepository.existsByOriginalUrlWithinOneMonth(
                url, DateUtils.getOneMonthAgo());

        if (isRecentlyVisited) {
            log.debug("[BfsFilterService.filterByRecentVisit] Filtered out URL (visited within 1 month): {}", url);
            return false;
        }

        return true;
    }

    /**
     * URL이 BFS 필터링 조건을 만족하는지 단일 체크
     * @param crawlingId 크롤링 세션 ID
     * @param currentBfsPath 현재 BFS 경로
     * @param url 체크할 URL
     * @return 필터링 조건을 만족하면 true
     */
    public boolean isUrlAllowedForBfs(String crawlingId, String currentBfsPath, String url) {
        String rootDomain = downloadQueueRepository.findRootDomainByCrawlingId(crawlingId);
        if (rootDomain == null) {
            return false;
        }

        return filterByRootDomain(url, rootDomain) &&
               filterByBfsPath(crawlingId, currentBfsPath, url) &&
               filterByRecentVisit(url);
    }
}