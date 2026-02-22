package urlshorten.domain.shortenurl

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object ShortenUrls : Table("shorten_urls") {
    val id = long("id")
    val shortenUrl = varchar("shorten_url", 255).uniqueIndex()
    val sourceUrl = varchar("source_url", 2083).uniqueIndex()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

data class ShortenUrl(
    val id: Long,
    val shortenUrl: String,
    val sourceUrl: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun create(id: Long, shortenUrl: String, sourceUrl: String): ShortenUrl {
            return ShortenUrl(
                id = id,
                shortenUrl = shortenUrl,
                sourceUrl = sourceUrl,
                createdAt = LocalDateTime.now()
            )
        }
    }
}
