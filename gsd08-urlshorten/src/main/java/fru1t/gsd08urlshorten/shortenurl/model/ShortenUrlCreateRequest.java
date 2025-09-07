package fru1t.gsd08urlshorten.shortenurl.model;

import fru1t.gsd08urlshorten.shortenurl.validation.ValidUrl;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortenUrlCreateRequest {
    @NotBlank(message = "URL은 필수입니다")
    @ValidUrl
    private String originalUrl;
}