package fru1t.gsd08urlshorten.shortenurl;

import fru1t.gsd08urlshorten.common.Snowflake;
import fru1t.gsd08urlshorten.common.util.Base62;
import fru1t.gsd08urlshorten.shortenurl.model.ShortenUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShortenUrlService {
    private final ShortenUrlRepository shortenUrlRepository;
    private final Snowflake snowflak = new Snowflake();
    private final Base62 base62 = new Base62();

    public String createShortenUrl(String longUrl) {
        return shortenUrlRepository.findBySourceUrl(longUrl)
                .map(ShortenUrl::getShortenUrl)
                .orElseGet(() -> {
                    long id = snowflak.nextId();
                    String shortUrl = base62.encode(id);
                    shortenUrlRepository.save(ShortenUrl.createShortenUrl(id, shortUrl, longUrl));
                    return shortUrl;
                });
    }

    public String readShortenUrl(String shortenUrl) {
        return shortenUrlRepository.findByShortenUrl(shortenUrl)
                .map(ShortenUrl::getSourceUrl)
                .orElseThrow(() -> new IllegalArgumentException("단축 URL을 찾을 수 없습니다: " + shortenUrl));
    }
}
