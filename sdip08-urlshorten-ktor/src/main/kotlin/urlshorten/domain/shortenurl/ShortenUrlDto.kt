package urlshorten.domain.shortenurl

import kotlinx.serialization.Serializable

@Serializable
data class ShortenUrlRequest(
    val originalUrl: String
) {
    fun validate(): Boolean {
        if (originalUrl.isBlank()) return false
        val normalized = if (originalUrl.startsWith("http")) originalUrl else "http://$originalUrl"
        return try {
            java.net.URL(normalized)
            true
        } catch (e: Exception) {
            false
        }
    }
}

@Serializable
data class ShortenUrlResponse(
    val shortUrl: String
)
