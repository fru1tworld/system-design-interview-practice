package htmldownloader.domain.downloadqueue

import htmldownloader.util.DateUtils
import htmldownloader.util.LinkExtractionUtils
import org.slf4j.LoggerFactory

class BfsFilterService(
    private val repository: DownloadQueueRepository
) {
    private val log = LoggerFactory.getLogger(BfsFilterService::class.java)

    fun filterUrlsForBfs(crawlingId: String, currentBfsPath: String, extractedUrls: List<String>): List<String> {
        val rootDomain = repository.findRootDomainByCrawlingId(crawlingId)
        if (rootDomain == null) {
            log.warn("[BfsFilterService.filterUrlsForBfs] Root domain not found for crawlingId: {}", crawlingId)
            return emptyList()
        }

        log.info("[BfsFilterService.filterUrlsForBfs] Filtering {} URLs for crawlingId: {}, rootDomain: {}",
            extractedUrls.size, crawlingId, rootDomain)

        val filteredUrls = extractedUrls
            .filter { url -> filterByRootDomain(url, rootDomain) }
            .filter { url -> filterByBfsPath(crawlingId, currentBfsPath, url) }
            .filter { url -> filterByRecentVisit(url) }

        log.info("[BfsFilterService.filterUrlsForBfs] Filtered result: {} URLs remaining from {} original URLs",
            filteredUrls.size, extractedUrls.size)

        return filteredUrls
    }

    private fun filterByRootDomain(url: String, rootDomain: String): Boolean {
        val urlDomain = LinkExtractionUtils.extractRootDomain(url)
        val isAllowed = rootDomain == urlDomain

        if (!isAllowed) {
            log.debug("[BfsFilterService.filterByRootDomain] Filtered out URL (different domain): {} (expected: {})",
                urlDomain, rootDomain)
        }

        return isAllowed
    }

    private fun filterByBfsPath(crawlingId: String, currentBfsPath: String, url: String): Boolean {
        val newBfsPath = "$currentBfsPath|$url"

        val isDuplicatePath = repository.existsByBfsPath(newBfsPath)
        val isDuplicateInSession = repository.existsByCrawlingIdAndBfsPath(crawlingId, newBfsPath)

        if (isDuplicatePath || isDuplicateInSession) {
            log.debug("[BfsFilterService.filterByBfsPath] Filtered out URL (duplicate path): {}", newBfsPath)
            return false
        }

        return true
    }

    private fun filterByRecentVisit(url: String): Boolean {
        val isRecentlyVisited = repository.existsByOriginalUrlWithinOneMonth(
            url, DateUtils.getOneMonthAgo()
        )

        if (isRecentlyVisited) {
            log.debug("[BfsFilterService.filterByRecentVisit] Filtered out URL (visited within 1 month): {}", url)
            return false
        }

        return true
    }

    fun isUrlAllowedForBfs(crawlingId: String, currentBfsPath: String, url: String): Boolean {
        val rootDomain = repository.findRootDomainByCrawlingId(crawlingId) ?: return false

        return filterByRootDomain(url, rootDomain) &&
                filterByBfsPath(crawlingId, currentBfsPath, url) &&
                filterByRecentVisit(url)
    }
}
