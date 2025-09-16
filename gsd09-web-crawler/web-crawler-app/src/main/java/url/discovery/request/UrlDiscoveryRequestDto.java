package url.discovery.request;

import url.discovery.config.UrlDiscoveryConstants;
import lombok.Getter;

@Getter
public class UrlDiscoveryRequestDto {
    private String startUrl;
    private Integer maxDepth;

    public Integer getMaxDepth() {
        return maxDepth != null ? maxDepth : UrlDiscoveryConstants.DEFAULT_MAX_DEPTH;
    }

    public static UrlDiscoveryRequestDto createForTest(String startUrl, Integer maxDepth) {
        UrlDiscoveryRequestDto dto = new UrlDiscoveryRequestDto();
        dto.startUrl = startUrl;
        dto.maxDepth = maxDepth;
        return dto;
    }
}
