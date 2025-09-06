package fru1t.gsd08urlshorten.shortenurl;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
public class ShortenUrl {
    @Id
    private Long id;
    private String shortenUrl;
    private String sourceUrl;
    private LocalDateTime creationAt;

    public static ShortenUrl createShortenUrl(Long id, String shortenUrl, String sourceUrl) {
        ShortenUrl shortenUrlEntity = new ShortenUrl();
        shortenUrlEntity.id = id;
        shortenUrlEntity.shortenUrl = shortenUrl;
        shortenUrlEntity.sourceUrl = sourceUrl;
        shortenUrlEntity.creationAt = LocalDateTime.now();
        return shortenUrlEntity;
    };
}
