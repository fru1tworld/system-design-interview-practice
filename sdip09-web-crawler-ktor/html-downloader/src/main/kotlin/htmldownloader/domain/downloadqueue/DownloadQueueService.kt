package htmldownloader.domain.downloadqueue

import event.UrlDiscoveryCreatePayload
import htmldownloader.kafka.UrlDiscoveryProducer
import htmldownloader.util.DateUtils
import htmldownloader.util.LinkExtractionUtils
import org.slf4j.LoggerFactory

class DownloadQueueService(
    private val repository: DownloadQueueRepository,
    private val htmlDownloadService: HtmlDownloadService,
    private val urlDiscoveryProducer: UrlDiscoveryProducer,
    private val bfsFilterService: BfsFilterService
) {
    private val log = LoggerFactory.getLogger(DownloadQueueService::class.java)

    suspend fun consumeUrlDiscovery(eventId: String, payload: UrlDiscoveryCreatePayload) {
        // 1. 1달 이내 방문 체크
        if (repository.existsByOriginalUrlWithinOneMonth(payload.url, DateUtils.getOneMonthAgo())) {
            log.info("[DownloadQueueService.consumeUrlDiscovery] URL visited within 1 month, skipping: {}", payload.url)
            return
        }

        // 2. bfsPath 중복 체크
        if (repository.existsByBfsPath(payload.bfsPath)) {
            log.info("[DownloadQueueService.consumeUrlDiscovery] BFS path already exists, skipping: {}", payload.bfsPath)
            return
        }

        // 3. DownloadQueue 생성 및 저장
        var downloadQueue = DownloadQueue.create(
            id = eventId,
            crawlingId = payload.crawlingId,
            originalUrl = payload.url,
            bfsPath = payload.bfsPath,
            depth = payload.depth,
            status = DownloadStatus.PENDING.name
        )

        val rootDomain = LinkExtractionUtils.extractRootDomain(payload.url)
        downloadQueue = downloadQueue.copy(rootDomain = rootDomain)

        repository.save(downloadQueue)
        if (rootDomain != null) {
            repository.updateRootDomain(eventId, rootDomain)
        }
        log.info("[DownloadQueueService.consumeUrlDiscovery] Successfully saved to download_queue: id={}", downloadQueue.id)

        // 4. 다운로드 처리
        processDownloadQueue(downloadQueue, payload)
    }

    private suspend fun processDownloadQueue(downloadQueue: DownloadQueue, payload: UrlDiscoveryCreatePayload) {
        try {
            repository.updateStatus(downloadQueue.id, DownloadStatus.PROCESSING.name)

            val html = htmlDownloadService.downloadHtml(payload.url)
            val extractedLinks = LinkExtractionUtils.extractLinks(html, payload.url)

            if (extractedLinks.isNotEmpty()) {
                // BFS 필터링 적용
                val filteredLinks = bfsFilterService.filterUrlsForBfs(
                    payload.crawlingId,
                    payload.bfsPath,
                    extractedLinks
                )

                if (filteredLinks.isNotEmpty()) {
                    urlDiscoveryProducer.produceUrlDiscoveryEvents(
                        crawlingId = payload.crawlingId,
                        currentDepth = payload.depth,
                        maxDepth = payload.maxDepth,
                        currentBfsPath = payload.bfsPath,
                        discoveredUrls = filteredLinks
                    )
                    log.info("[DownloadQueueService.processDownloadQueue] Produced {} filtered next-depth events for URL: {} (filtered from {} original links)",
                        filteredLinks.size, payload.url, extractedLinks.size)
                } else {
                    log.info("[DownloadQueueService.processDownloadQueue] No valid links after filtering for URL: {} (had {} original links)",
                        payload.url, extractedLinks.size)
                }
            } else {
                log.info("[DownloadQueueService.processDownloadQueue] No links extracted from URL: {}", payload.url)
            }

            repository.updateStatus(downloadQueue.id, DownloadStatus.COMPLETED.name)

        } catch (e: Exception) {
            log.error("[DownloadQueueService.processDownloadQueue] Failed to process download queue: id={}, url={}",
                downloadQueue.id, payload.url, e)
            repository.updateStatus(downloadQueue.id, DownloadStatus.FAILED.name)
        }
    }
}
