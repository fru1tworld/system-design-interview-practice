package event

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Event<T : EventPayload>(
    val eventId: String,
    val type: EventType,
    val payload: T
) {
    fun toJson(): String = json.encodeToString(this)

    companion object {
        @PublishedApi
        internal val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = false
        }

        fun <T : EventPayload> of(eventId: String, type: EventType, payload: T): Event<T> =
            Event(eventId = eventId, type = type, payload = payload)

        inline fun <reified T : EventPayload> fromJson(jsonString: String): Event<T>? = runCatching {
            json.decodeFromString<Event<T>>(jsonString)
        }.getOrNull()

        /**
         * Generic deserialization that returns Event with dynamic payload type
         */
        fun fromJsonDynamic(jsonString: String): Event<EventPayload>? {
            val rawEvent = runCatching {
                json.decodeFromString<EventRaw>(jsonString)
            }.getOrNull() ?: return null

            val eventType = EventType.fromString(rawEvent.type) ?: return null

            val payload: EventPayload = when (eventType) {
                EventType.URL_DISCOVERY_CREATE -> json.decodeFromString<UrlDiscoveryCreatePayload>(
                    json.encodeToString(rawEvent.payload)
                )
            }

            return Event(
                eventId = rawEvent.eventId,
                type = eventType,
                payload = payload
            )
        }
    }
}

@Serializable
internal data class EventRaw(
    val eventId: String,
    val type: String,
    val payload: kotlinx.serialization.json.JsonElement
)
