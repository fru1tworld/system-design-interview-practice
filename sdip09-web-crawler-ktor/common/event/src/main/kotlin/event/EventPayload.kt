package event

import kotlinx.serialization.Serializable

/**
 * Marker interface for event payloads.
 * All payload classes must be @Serializable.
 */
@Serializable
sealed interface EventPayload
