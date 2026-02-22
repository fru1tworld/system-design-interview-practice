package urlshorten.domain.shortenurl

import urlshorten.common.Base62
import urlshorten.common.Snowflake
import urlshorten.plugins.NotFoundException

class ShortenUrlService(
    private val repository: ShortenUrlRepository
) {
    private val snowflake = Snowflake()

    fun createShortenUrl(longUrl: String): String {
        // 이미 존재하는 URL인 경우 기존 단축 URL 반환
        repository.findBySourceUrl(longUrl)?.let {
            return it.shortenUrl
        }

        // 새로운 단축 URL 생성
        val id = snowflake.nextId()
        val shortUrl = Base62.encode(id)

        val shortenUrl = ShortenUrl.create(id, shortUrl, longUrl)
        repository.save(shortenUrl)

        return shortUrl
    }

    fun readShortenUrl(shortenUrl: String): String {
        return repository.findByShortenUrl(shortenUrl)?.sourceUrl
            ?: throw NotFoundException("단축 URL을 찾을 수 없습니다: $shortenUrl")
    }
}
