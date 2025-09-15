package fru1t.webcrawler.app.url.discovery.service;

import fru1t.webcrawler.app.url.discovery.producer.UrlDiscoveryEventProducer;
import fru1t.webcrawler.app.url.discovery.request.UrlDiscoveryBatchResponseDto;
import fru1t.webcrawler.app.url.discovery.request.UrlDiscoveryRequestDto;
import fru1t.webcrawler.app.url.discovery.response.UrlDiscoveryErrorDto;
import fru1t.webcrawler.app.url.discovery.response.UrlDiscoveryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlDiscoveryService {
    private final UrlDiscoveryEventProducer urlDiscoveryEventProducer;

    public UrlDiscoveryResponseDto createUrlDiscovery(UrlDiscoveryRequestDto requestDto) {
        try {
            String crawlingId = urlDiscoveryEventProducer.publishUrlDiscoveryCreate(requestDto);

            return UrlDiscoveryResponseDto.create(crawlingId, requestDto.getStartUrl(), requestDto.getMaxDepth());

        } catch (Exception e) {
            log.error("[UrlDiscoveryService.createUrlDiscovery] url={}", requestDto.getStartUrl(), e);
            throw e;
        }
    }

    public UrlDiscoveryBatchResponseDto createUrlDiscoveries(List<UrlDiscoveryRequestDto> requestDtos) {
        List<UrlDiscoveryResponseDto> successResults = new ArrayList<>();
        List<UrlDiscoveryErrorDto> errors = new ArrayList<>();

        for (UrlDiscoveryRequestDto requestDto : requestDtos) {
            try {
                String crawlingId = urlDiscoveryEventProducer.publishUrlDiscoveryCreate(requestDto);

                successResults.add(UrlDiscoveryResponseDto.create(crawlingId, requestDto.getStartUrl(), requestDto.getMaxDepth()));

            } catch (Exception e) {
                log.error("[UrlDiscoveryService.createUrlDiscoveries] url={}", requestDto.getStartUrl(), e);

                errors.add(UrlDiscoveryErrorDto.create(requestDto.getStartUrl(), e.getMessage()));
            }
        }

        if (successResults.isEmpty()) {
            throw new RuntimeException("All URL discovery requests failed");
        }

        return UrlDiscoveryBatchResponseDto.create(successResults, errors);
    }
}