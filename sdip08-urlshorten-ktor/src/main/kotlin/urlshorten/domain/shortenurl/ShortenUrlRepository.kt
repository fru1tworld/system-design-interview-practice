package urlshorten.domain.shortenurl

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ShortenUrlRepository(private val database: Database) {

    fun save(shortenUrl: ShortenUrl): ShortenUrl {
        transaction(database) {
            ShortenUrls.insert { row ->
                row[id] = shortenUrl.id
                row[ShortenUrls.shortenUrl] = shortenUrl.shortenUrl
                row[sourceUrl] = shortenUrl.sourceUrl
                row[createdAt] = shortenUrl.createdAt
            }
        }
        return shortenUrl
    }

    fun findByShortenUrl(shortenUrl: String): ShortenUrl? {
        return transaction(database) {
            ShortenUrls.selectAll()
                .where { ShortenUrls.shortenUrl eq shortenUrl }
                .map { it.toShortenUrl() }
                .singleOrNull()
        }
    }

    fun findBySourceUrl(sourceUrl: String): ShortenUrl? {
        return transaction(database) {
            ShortenUrls.selectAll()
                .where { ShortenUrls.sourceUrl eq sourceUrl }
                .map { it.toShortenUrl() }
                .singleOrNull()
        }
    }

    private fun ResultRow.toShortenUrl(): ShortenUrl = ShortenUrl(
        id = this[ShortenUrls.id],
        shortenUrl = this[ShortenUrls.shortenUrl],
        sourceUrl = this[ShortenUrls.sourceUrl],
        createdAt = this[ShortenUrls.createdAt]
    )
}
