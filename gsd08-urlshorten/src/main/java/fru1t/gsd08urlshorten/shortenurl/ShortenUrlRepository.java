package fru1t.gsd08urlshorten.shortenurl;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShortenUrlRepository extends JpaRepository<ShortenUrl, Long> {
    Optional<ShortenUrl> findByShortenUrl(String shortenUrl);
}
