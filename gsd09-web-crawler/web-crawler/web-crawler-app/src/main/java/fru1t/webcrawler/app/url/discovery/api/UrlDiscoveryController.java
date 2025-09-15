package fru1t.webcrawler.app.url.discovery.api;

import fru1t.webcrawler.app.url.discovery.request.UrlDiscoveryRequestDto;
import fru1t.webcrawler.app.url.discovery.request.UrlDiscoveryBatchResponseDto;
import fru1t.webcrawler.app.url.discovery.response.UrlDiscoveryResponseDto;
import fru1t.webcrawler.app.url.discovery.service.UrlDiscoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UrlDiscoveryController {
    private final UrlDiscoveryService urlDiscoveryService;

    @PostMapping("/api/v1/url-discovery")
    public UrlDiscoveryResponseDto createUrlDiscovery(@RequestBody UrlDiscoveryRequestDto requestDto) {
        return urlDiscoveryService.createUrlDiscovery(requestDto);
    }

    @PostMapping("/api/v1/url-discovery/batch")
    public UrlDiscoveryBatchResponseDto createUrlDiscoveries(@RequestBody List<UrlDiscoveryRequestDto> requestDtos) {
        return urlDiscoveryService.createUrlDiscoveries(requestDtos);
    }
}