package htmldownloader.domain.downloadqueue

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.slf4j.LoggerFactory

class HtmlDownloadService(
    private val httpClient: HttpClient
) {
    private val log = LoggerFactory.getLogger(HtmlDownloadService::class.java)

    suspend fun downloadHtml(url: String): String {
        return try {
            val response: HttpResponse = httpClient.get(url)
            response.bodyAsText()
        } catch (e: Exception) {
            log.error("[HtmlDownloadService.downloadHtml] Failed to download HTML from URL: {}", url, e)
            throw RuntimeException("Failed to download HTML", e)
        }
    }
}
