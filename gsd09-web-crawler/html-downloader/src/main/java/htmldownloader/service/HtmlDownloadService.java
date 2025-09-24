package htmldownloader.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class HtmlDownloadService {

    private final WebClient webClient;

    public HtmlDownloadService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }

    public String downloadHtml(String url) {
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
        } catch (Exception e) {
            log.error("[HtmlDownloadService.downloadHtml] Failed to download HTML from URL: {}", url, e);
            throw new RuntimeException("Failed to download HTML", e);
        }
    }

    public Mono<String> downloadHtmlAsync(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(error ->
                    log.error("[HtmlDownloadService.downloadHtmlAsync] Failed to download HTML from URL: {}", url, error)
                );
    }
}