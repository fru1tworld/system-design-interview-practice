package event

import kotlinx.serialization.Serializable

@Serializable
data class UrlDiscoveryCreatePayload(
    val crawlingId: String,
    val depth: Int,
    val maxDepth: Int,
    val url: String,
    val bfsPath: String
) : EventPayload {

    companion object {
        fun create(
            crawlingId: String,
            depth: Int,
            maxDepth: Int,
            url: String,
            bfsPath: String
        ): UrlDiscoveryCreatePayload = UrlDiscoveryCreatePayload(
            crawlingId = crawlingId,
            depth = depth,
            maxDepth = maxDepth,
            url = url,
            bfsPath = bfsPath
        )
    }
}
