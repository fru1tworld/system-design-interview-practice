package htmldownloader.util

import org.slf4j.LoggerFactory
import java.net.MalformedURLException
import java.net.URL

object LinkExtractionUtils {
    private val log = LoggerFactory.getLogger(LinkExtractionUtils::class.java)

    private val HREF_PATTERN = Regex(
        """<a\s+[^>]*href\s*=\s*["']([^"']+)["'][^>]*>""",
        RegexOption.IGNORE_CASE
    )

    fun extractLinks(html: String?, baseUrl: String): List<String> {
        if (html.isNullOrBlank()) {
            return emptyList()
        }

        val uniqueUrls = mutableSetOf<String>()

        HREF_PATTERN.findAll(html).forEach { matchResult ->
            val href = matchResult.groupValues[1]
            val absoluteUrl = convertToAbsoluteUrl(href, baseUrl)

            if (absoluteUrl != null && isValidUrl(absoluteUrl)) {
                uniqueUrls.add(absoluteUrl)
            }
        }

        log.info("[LinkExtractionUtils.extractLinks] Extracted {} unique links from HTML", uniqueUrls.size)
        return uniqueUrls.toList()
    }

    private fun convertToAbsoluteUrl(href: String, baseUrl: String): String? {
        return try {
            when {
                href.startsWith("http://") || href.startsWith("https://") -> href
                href.startsWith("#") || href.startsWith("javascript:") || href.startsWith("mailto:") -> null
                else -> {
                    val base = URL(baseUrl)
                    when {
                        href.startsWith("/") -> {
                            val port = if (base.port != -1) ":${base.port}" else ""
                            "${base.protocol}://${base.host}$port$href"
                        }
                        else -> URL(base, href).toString()
                    }
                }
            }
        } catch (e: MalformedURLException) {
            log.warn("[LinkExtractionUtils.convertToAbsoluteUrl] Failed to convert href: href={}, baseUrl={}", href, baseUrl)
            null
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val urlObj = URL(url)
            urlObj.protocol == "http" || urlObj.protocol == "https"
        } catch (e: MalformedURLException) {
            false
        }
    }

    fun extractRootDomain(url: String): String? {
        return try {
            val urlObj = URL(url)
            val host = urlObj.host
            if (host.startsWith("www.")) {
                host.substring(4)
            } else {
                host
            }
        } catch (e: MalformedURLException) {
            log.warn("[LinkExtractionUtils.extractRootDomain] Failed to extract root domain from URL: {}", url)
            null
        }
    }
}
