package event.event;

import event.event.payload.UrlDiscoveryCreatePayload;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;


class EventTest {
    @Test
    void serde(){
      UrlDiscoveryCreatePayload payload = UrlDiscoveryCreatePayload.create(
                "AAA",
                1,
              1,
               "http://www.google.com/");
      
        Event<EventPayload> event = Event.of(
                12L,
                EventType.URL_DISCOVERY_CREATE,
                payload
        );
        String json = event.toJson();
        System.out.println("json = " + json);
        Event<EventPayload> result = Event.fromJson(json);


        assertThat(result.getEventId()).isEqualTo(event.getEventId());
        assertThat(result.getType()).isEqualTo(event.getType());
        assertThat(result.getPayload()).isInstanceOf(payload.getClass());

        UrlDiscoveryCreatePayload payloadResponse = (UrlDiscoveryCreatePayload) result.getPayload();
        
        assertThat(payloadResponse.getCrawlingId()).isEqualTo(payload.getCrawlingId());
        assertThat(payloadResponse.getDepth()).isEqualTo(payload.getDepth());
        assertThat(payloadResponse.getMaxDepth()).isEqualTo(payload.getMaxDepth());
        System.out.println("payloadResponse = " + payloadResponse);
    }

}