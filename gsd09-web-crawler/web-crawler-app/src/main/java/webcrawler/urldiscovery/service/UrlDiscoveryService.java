package webcrawler.urldiscovery.service;

import webcrawler.urldiscovery.producer.UrlDiscoveryEventProducer;
import webcrawler.urldiscovery.request.UrlDiscoveryBatchResponseDto;
import webcrawler.urldiscovery.request.UrlDiscoveryRequestDto;
import webcrawler.urldiscovery.response.UrlDiscoveryErrorDto;
import webcrawler.urldiscovery.response.UrlDiscoveryResponseDto;
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
            return urlDiscoveryEventProducer.publishUrlDiscoveryCreate(requestDto);

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
                UrlDiscoveryResponseDto responseDto = urlDiscoveryEventProducer.publishUrlDiscoveryCreate(requestDto);
                successResults.add(responseDto);

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