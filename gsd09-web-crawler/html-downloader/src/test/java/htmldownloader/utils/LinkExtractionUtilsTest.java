package htmldownloader.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LinkExtractionUtilsTest {

    @Test
    void shouldExtractLinksFromHtml() {
        // Given
        String html = """
                <html>
                <body>
                    <a href="https://example.com/page1">Link 1</a>
                    <a href="/page2">Relative Link</a>
                    <a href="../page3">Parent Relative Link</a>
                    <a href="mailto:test@example.com">Email Link</a>
                    <a href="#section1">Anchor Link</a>
                    <a href="javascript:void(0)">JavaScript Link</a>
                </body>
                </html>
                """;
        String baseUrl = "https://example.com/current/page";

        // When
        List<String> links = LinkExtractionUtils.extractLinks(html, baseUrl);

        // Then
        assertThat(links).containsExactlyInAnyOrder(
                "https://example.com/page1",
                "https://example.com/page2",
                "https://example.com/page3"
        );
    }

    @Test
    void shouldHandleEmptyHtml() {
        // Given
        String html = "";
        String baseUrl = "https://example.com";

        // When
        List<String> links = LinkExtractionUtils.extractLinks(html, baseUrl);

        // Then
        assertThat(links).isEmpty();
    }

    @Test
    void shouldExtractRootDomain() {
        // Given & When & Then
        assertThat(LinkExtractionUtils.extractRootDomain("https://www.example.com/path"))
                .isEqualTo("example.com");
        assertThat(LinkExtractionUtils.extractRootDomain("https://example.com"))
                .isEqualTo("example.com");
        assertThat(LinkExtractionUtils.extractRootDomain("http://subdomain.example.com"))
                .isEqualTo("subdomain.example.com");
    }

    @Test
    void shouldHandleInvalidUrl() {
        // Given & When & Then
        assertThat(LinkExtractionUtils.extractRootDomain("invalid-url"))
                .isNull();
    }
}