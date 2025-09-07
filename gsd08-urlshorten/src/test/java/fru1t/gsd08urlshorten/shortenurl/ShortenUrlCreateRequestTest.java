package fru1t.gsd08urlshorten.shortenurl;

import fru1t.gsd08urlshorten.shortenurl.model.ShortenUrlCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ShortenUrlCreateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRequest() {
        ShortenUrlCreateRequest request = new ShortenUrlCreateRequest();
        request.setOriginalUrl("https://example.com");

        Set<ConstraintViolation<ShortenUrlCreateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidRequestWithoutProtocol() {
        ShortenUrlCreateRequest request = new ShortenUrlCreateRequest();
        request.setOriginalUrl("example.com");

        Set<ConstraintViolation<ShortenUrlCreateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullUrl() {
        ShortenUrlCreateRequest request = new ShortenUrlCreateRequest();
        request.setOriginalUrl(null);

        Set<ConstraintViolation<ShortenUrlCreateRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
        
        boolean hasRequiredMessage = violations.stream()
            .anyMatch(v -> v.getMessage().equals("URL은 필수입니다"));
        assertTrue(hasRequiredMessage);
    }

    @Test
    void testEmptyUrl() {
        ShortenUrlCreateRequest request = new ShortenUrlCreateRequest();
        request.setOriginalUrl("");

        Set<ConstraintViolation<ShortenUrlCreateRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size()); // @NotBlank and @ValidUrl both trigger
        
        boolean hasRequiredMessage = violations.stream()
            .anyMatch(v -> v.getMessage().equals("URL은 필수입니다"));
        assertTrue(hasRequiredMessage);
    }

    @Test
    void testBlankUrl() {
        ShortenUrlCreateRequest request = new ShortenUrlCreateRequest();
        request.setOriginalUrl("   ");

        Set<ConstraintViolation<ShortenUrlCreateRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
        
        boolean hasRequiredMessage = violations.stream()
            .anyMatch(v -> v.getMessage().equals("URL은 필수입니다"));
        assertTrue(hasRequiredMessage);
    }

    @Test
    void testInvalidUrl() {
        ShortenUrlCreateRequest request = new ShortenUrlCreateRequest();
        request.setOriginalUrl("ftp://invalid-protocol.com");

        Set<ConstraintViolation<ShortenUrlCreateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        
        ConstraintViolation<ShortenUrlCreateRequest> violation = violations.iterator().next();
        assertEquals("잘못된 URL 형식입니다", violation.getMessage());
    }

    @Test
    void testComplexValidUrls() {
        String[] validUrls = {
            "https://www.example.com",
            "http://subdomain.example.com:8080/path?query=value",
            "https://api.example.com/v1/users/123#section",
            "http://localhost:3000",
            "https://127.0.0.1:8080",
            "example.com/path/to/resource"
        };

        for (String url : validUrls) {
            ShortenUrlCreateRequest request = new ShortenUrlCreateRequest();
            request.setOriginalUrl(url);

            Set<ConstraintViolation<ShortenUrlCreateRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "URL should be valid: " + url);
        }
    }
}