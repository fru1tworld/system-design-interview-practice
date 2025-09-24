package webcrawler.urldiscovery.response;

import lombok.Getter;

@Getter
public class UrlDiscoveryResponseDto {
    private String crawlingId;
    private String url;
    private Integer maxDepth;

    public static UrlDiscoveryResponseDto create(String crawlingId, String url, Integer maxDepth) {
        UrlDiscoveryResponseDto response = new UrlDiscoveryResponseDto();
        response.crawlingId = crawlingId;
        response.url = url;
        response.maxDepth = maxDepth;
        return response;
    }
}