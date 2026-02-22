package event

import kotlinx.serialization.Serializable

@Serializable
enum class EventType(val topic: String) {
    URL_DISCOVERY_CREATE(Topic.URL_DISCOVERY_CREATE);

    object Topic {
        const val URL_DISCOVERY_CREATE = "fru1tworld-webcrawling-url-discovery-v1"
    }

    companion object {
        fun fromString(type: String): EventType? = runCatching {
            valueOf(type)
        }.getOrNull()
    }
}
