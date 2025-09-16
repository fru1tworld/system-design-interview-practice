package event.event.payload;

import event.event.EventPayload;
import lombok.Getter;

@Getter
public class UrlDiscoveryCreatePayload implements EventPayload {
    private String crawlingId;
    private Integer depth;
    private Integer maxDepth;
    private String url;

    public static UrlDiscoveryCreatePayload create(String crawlingId, Integer depth, Integer maxDepth, String url) {
        UrlDiscoveryCreatePayload payload = new UrlDiscoveryCreatePayload();
        payload.crawlingId = crawlingId;
        payload.depth = depth;
        payload.maxDepth = maxDepth;
        payload.url = url;
        return payload;
    }
}
