package fru1t.gsd08urlshorten.shortenurl;

import fru1t.gsd08urlshorten.shortenurl.validation.UrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class UrlValidatorTest {

    private UrlValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new UrlValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testValidUrls() {
        assertTrue(validator.isValid("http://example.com", context));
        assertTrue(validator.isValid("https://example.com", context));
        assertTrue(validator.isValid("http://www.example.com", context));
        assertTrue(validator.isValid("https://www.example.com", context));
        assertTrue(validator.isValid("http://example.com/path", context));
        assertTrue(validator.isValid("https://example.com/path/to/resource", context));
        assertTrue(validator.isValid("http://example.com:8080", context));
        assertTrue(validator.isValid("https://example.com:8443", context));
        assertTrue(validator.isValid("http://example.com?query=value", context));
        assertTrue(validator.isValid("https://example.com#fragment", context));
    }

    @Test
    void testValidUrlsWithoutProtocol() {
        assertTrue(validator.isValid("example.com", context));
        assertTrue(validator.isValid("www.example.com", context));
        assertTrue(validator.isValid("subdomain.example.com", context));
        assertTrue(validator.isValid("example.com/path", context));
        assertTrue(validator.isValid("example.com:8080", context));
    }

    @Test
    void testInvalidUrls() {
        String[] invalidUrls = {
            null,
            "",
            "   ",
            "ftp://example.com", // FTP not allowed
            "file:///path/to/file", // File protocol not allowed
            "javascript:alert('xss')", // JavaScript not allowed
            "http://",
            "https://"
        };
        
        for (String url : invalidUrls) {
            boolean isValid = validator.isValid(url, context);
            assertFalse(isValid, "URL should be invalid but was accepted: " + url);
        }
    }

    @Test
    void testComplexValidUrls() {
        String[] urls = {
            "https://subdomain.example.com:8443/path/to/resource?query=value&another=param#fragment",
            "http://localhost:3000",
            "https://127.0.0.1:8080",
            "http://192.168.1.1",
            "https://api.example.com/v1/users/123"
        };
        
        for (String url : urls) {
            boolean isValid = validator.isValid(url, context);
            assertTrue(isValid, "URL should be valid but was rejected: " + url);
        }
    }
}