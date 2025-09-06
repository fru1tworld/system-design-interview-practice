package fru1t.gsd08urlshorten.shortenurl;

import fru1t.gsd08urlshorten.common.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.seruco.encoding.base62.Base62;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShortenUrlService {
    private final ShortenUrlRepository shortenUrlRepository;
    private final Snowflake snowflak = new Snowflake();
    private final Base62 base62 = Base62.createInstance();

    public String createShortenUrl(String longUrl) {
        long id = snowflak.nextId();
        String shortUrl = new String(base62.encode(String.valueOf(id).getBytes(StandardCharsets.UTF_8)));
        shortenUrlRepository.save(new ShortenUrl().createShortenUrl(snowflak.nextId(), shortUrl, longUrl));
        return shortUrl;
    }
    public String readShortenUrl(String shortenUrl) {
        Optional<ShortenUrl> shortenUrlEntity = shortenUrlRepository.findByShortenUrl(shortenUrl);
        if (shortenUrlEntity.isPresent()) {
            return shortenUrlEntity.get().getSourceUrl();
        }else{
            throw new IllegalArgumentException("단축 URL을 찾을 수 없습니다: " + shortenUrl);
        }
    }
}
