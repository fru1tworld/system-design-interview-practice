package fru1t.gsd08urlshorten.shortenurl.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
public class ShortenUrl {
    @Id
    private Long id;
    private String shortenUrl;

    @Column(length = 2083)
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
