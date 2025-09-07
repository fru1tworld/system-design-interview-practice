package fru1t.gsd08urlshorten.shortenurl.validation;

import jakarta.validation.*;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.validator.routines.UrlValidator.ALLOW_LOCAL_URLS;

@Slf4j
public class UrlValidator implements ConstraintValidator<ValidUrl, String> {

    private final org.apache.commons.validator.routines.UrlValidator apacheValidator =
            new org.apache.commons.validator.routines.UrlValidator(
                    new String[]{"http", "https"},
                    ALLOW_LOCAL_URLS
            );

    @Override
    public void initialize(ValidUrl constraintAnnotation) {
    }

    @Override
    public boolean isValid(String url, ConstraintValidatorContext context) {
        if (url == null || url.isBlank()) return false;
        String trimmed = url.trim();
        String normalizedUrl = trimmed.startsWith("http") ? trimmed : "http://" + trimmed;
        return apacheValidator.isValid(normalizedUrl);
    }

}