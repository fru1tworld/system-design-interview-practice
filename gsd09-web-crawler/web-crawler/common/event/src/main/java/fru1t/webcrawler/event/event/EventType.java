package fru1t.webcrawler.event.event;

import fru1t.webcrawler.event.event.payload.UrlDiscoveryCreatePayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    URL_DISCOVERY_CREATE(UrlDiscoveryCreatePayload.class, Topic.URL_DISCOVERY_CREATE);

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    public static EventType from(String type){
        try{
            return valueOf(type);
        } catch ( Exception e ){
            log.error("[EventType.from] type={}", type, e);
            return null ;
        }
    }

    public static class Topic{
        public static final String URL_DISCOVERY_CREATE = "fru1tworld-webcrawling-url-discovery-v1";
    }
}
