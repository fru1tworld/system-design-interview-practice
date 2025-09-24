package htmldownloader.service;

import event.event.payload.UrlDiscoveryCreatePayload;
import htmldownloader.entity.DownloadQueue;
import htmldownloader.producer.UrlDiscoveryProducer;
import htmldownloader.repository.DownloadQueueRepository;
import htmldownloader.utils.DateUtils;
import htmldownloader.utils.LinkExtractionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DownloadQueueService {
    private final DownloadQueueRepository downloadQueueRepository;
    private final HtmlDownloadService htmlDownloadService;
    private final UrlDiscoveryProducer urlDiscoveryProducer;
    private final BfsFilterService bfsFilterService;

    public void consumeUrlDiscovery(String eventId, UrlDiscoveryCreatePayload payload){
        // 1. 1달 이내 방문 체크 - 이미 방문한 URL은 처리하지 않음
        if (downloadQueueRepository.existsByOriginalUrlWithinOneMonth(payload.getUrl(), DateUtils.getOneMonthAgo())) {
            log.info("[DownloadQueueService.consumeUrlDiscovery] URL visited within 1 month, skipping: {}", payload.getUrl());
            return;
        }

        // 2. bfsPath 중복 체크 - 이미 탐색한 경로는 처리하지 않음
        if (downloadQueueRepository.existsByBfsPath(payload.getBfsPath())) {
            log.info("[DownloadQueueService.consumeUrlDiscovery] BFS path already exists, skipping: {}", payload.getBfsPath());
            return;
        }

        DownloadQueue downloadQueue = DownloadQueue.create(
                eventId,
                payload.getCrawlingId(),
                payload.getUrl(),
                payload.getBfsPath(),
                payload.getDepth(),
                "PENDING"
        );
        downloadQueue.setRootDomain(LinkExtractionUtils.extractRootDomain(payload.getUrl()));
        downloadQueueRepository.save(downloadQueue);
        log.info("[DownloadQueueService.consumeUrlDiscovery] Successfully saved to download_queue: id={}", downloadQueue.getId());

        processDownloadQueue(downloadQueue, payload);
    }

    private void processDownloadQueue(DownloadQueue downloadQueue, UrlDiscoveryCreatePayload payload) {
        try {
            updateStatus(downloadQueue.getId(), "PROCESSING");

            String html = htmlDownloadService.downloadHtml(payload.getUrl());
            List<String> extractedLinks = LinkExtractionUtils.extractLinks(html, payload.getUrl());

            if (!extractedLinks.isEmpty()) {
                // BFS 필터링 적용: rootDomain, bfsPath 중복, 1달 이내 방문 체크
                List<String> filteredLinks = bfsFilterService.filterUrlsForBfs(
                        payload.getCrawlingId(),
                        payload.getBfsPath(),
                        extractedLinks
                );

                if (!filteredLinks.isEmpty()) {
                    urlDiscoveryProducer.produceUrlDiscoveryEvents(
                            payload.getCrawlingId(),
                            payload.getDepth(),
                            payload.getMaxDepth(),
                            payload.getBfsPath(),
                            filteredLinks
                    );
                    log.info("[DownloadQueueService.processDownloadQueue] Produced {} filtered next-depth events for URL: {} (filtered from {} original links)",
                            filteredLinks.size(), payload.getUrl(), extractedLinks.size());
                } else {
                    log.info("[DownloadQueueService.processDownloadQueue] No valid links after filtering for URL: {} (had {} original links)",
                            payload.getUrl(), extractedLinks.size());
                }
            } else {
                log.info("[DownloadQueueService.processDownloadQueue] No links extracted from URL: {}", payload.getUrl());
            }

            updateStatus(downloadQueue.getId(), "COMPLETED");

        } catch (Exception e) {
            log.error("[DownloadQueueService.processDownloadQueue] Failed to process download queue: id={}, url={}",
                    downloadQueue.getId(), payload.getUrl(), e);
            updateStatus(downloadQueue.getId(), "FAILED");
        }
    }

    private void updateStatus(String id, String status) {
        DownloadQueue downloadQueue = downloadQueueRepository.findById(id).orElse(null);
        if (downloadQueue != null) {
            downloadQueue.setStatus(status);
            downloadQueue.setUpdatedAt(LocalDateTime.now());
            downloadQueueRepository.save(downloadQueue);
        }
    }
}
