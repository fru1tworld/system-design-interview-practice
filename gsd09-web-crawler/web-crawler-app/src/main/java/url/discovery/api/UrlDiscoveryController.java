package url.discovery.api;

import url.discovery.request.UrlDiscoveryRequestDto;
import url.discovery.request.UrlDiscoveryBatchResponseDto;
import url.discovery.response.UrlDiscoveryResponseDto;
import url.discovery.service.UrlDiscoveryService;
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