package event.event;

import ch.qos.logback.core.joran.spi.EventPlayer;
import event.dataserializer.Dataserializer;
import lombok.Getter;

@Getter
public class Event<T extends EventPayload> {
    private Long eventId;
    private EventType type;
    private T payload;

    public static Event<EventPayload> of(Long eventId, EventType type, EventPayload payload) {
    Event<EventPayload> event = new Event<>();
    event.eventId = eventId;
    event.type = type;
    event.payload = payload;
    return event;
    }

    public String toJson(){
        return Dataserializer.serialize(this);
    }

    public static Event<EventPayload> fromJson(String json){
        EventRaw eventRaw = Dataserializer.deserialize(json, EventRaw.class);
        if(eventRaw == null){
            return null;
        }
        Event<EventPayload> event = new Event<>();
        event.eventId = eventRaw.getEventId();
        event.type = EventType.from(eventRaw.getType());
        event.payload = Dataserializer.deserialize(eventRaw.getPayload(), event.type.getPayloadClass());
        return event;
    }

    @Getter
    private static class EventRaw{
        private Long eventId;
        private String type;
        private Object payload;
    }
}
