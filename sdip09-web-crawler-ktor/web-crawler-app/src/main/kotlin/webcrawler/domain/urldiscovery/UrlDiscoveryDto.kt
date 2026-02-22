package webcrawler.domain.urldiscovery

import kotlinx.serialization.Serializable

object UrlDiscoveryConstants {
    const val DEFAULT_MAX_DEPTH = 10
}

@Serializable
data class UrlDiscoveryRequestDto(
    val startUrl: String,
    val maxDepth: Int? = null
) {
    fun getEffectiveMaxDepth(): Int = maxDepth ?: UrlDiscoveryConstants.DEFAULT_MAX_DEPTH
}

@Serializable
data class UrlDiscoveryResponseDto(
    val crawlingId: String,
    val url: String,
    val maxDepth: Int
) {
    companion object {
        fun create(crawlingId: String, url: String, maxDepth: Int) =
            UrlDiscoveryResponseDto(crawlingId, url, maxDepth)
    }
}

@Serializable
data class UrlDiscoveryErrorDto(
    val url: String,
    val errorMessage: String
) {
    companion object {
        fun create(url: String, errorMessage: String) =
            UrlDiscoveryErrorDto(url, errorMessage)
    }
}

@Serializable
data class UrlDiscoveryBatchResponseDto(
    val successResults: List<UrlDiscoveryResponseDto>,
    val errors: List<UrlDiscoveryErrorDto>
) {
    companion object {
        fun create(
            successResults: List<UrlDiscoveryResponseDto>,
            errors: List<UrlDiscoveryErrorDto>
        ) = UrlDiscoveryBatchResponseDto(successResults, errors)
    }
}
