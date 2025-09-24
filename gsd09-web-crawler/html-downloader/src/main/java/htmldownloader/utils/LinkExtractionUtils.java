package htmldownloader.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkExtractionUtils {

    private static final Pattern HREF_PATTERN = Pattern.compile(
            "<a\\s+[^>]*href\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    public static List<String> extractLinks(String html, String baseUrl) {
        if (html == null || html.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> uniqueUrls = new HashSet<>();
        Matcher matcher = HREF_PATTERN.matcher(html);

        while (matcher.find()) {
            String href = matcher.group(1);
            String absoluteUrl = convertToAbsoluteUrl(href, baseUrl);

            if (absoluteUrl != null && isValidUrl(absoluteUrl)) {
                uniqueUrls.add(absoluteUrl);
            }
        }

        log.info("[LinkExtractionUtils.extractLinks] Extracted {} unique links from HTML", uniqueUrls.size());
        return new ArrayList<>(uniqueUrls);
    }

    private static String convertToAbsoluteUrl(String href, String baseUrl) {
        try {
            if (href.startsWith("http://") || href.startsWith("https://")) {
                return href;
            }

            URL base = new URL(baseUrl);

            if (href.startsWith("/")) {
                return base.getProtocol() + "://" + base.getHost() +
                       (base.getPort() != -1 ? ":" + base.getPort() : "") + href;
            }

            if (href.startsWith("../")) {
                return new URL(base, href).toString();
            }

            if (href.startsWith("./")) {
                return new URL(base, href).toString();
            }

            if (href.startsWith("#") || href.startsWith("javascript:") || href.startsWith("mailto:")) {
                return null;
            }

            return new URL(base, href).toString();

        } catch (MalformedURLException e) {
            log.warn("[LinkExtractionUtils.convertToAbsoluteUrl] Failed to convert href to absolute URL: href={}, baseUrl={}", href, baseUrl);
            return null;
        }
    }

    private static boolean isValidUrl(String url) {
        try {
            URL urlObj = new URL(url);
            return "http".equals(urlObj.getProtocol()) || "https".equals(urlObj.getProtocol());
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String extractRootDomain(String url) {
        try {
            URL urlObj = new URL(url);
            String host = urlObj.getHost();

            if (host.startsWith("www.")) {
                return host.substring(4);
            }

            return host;
        } catch (MalformedURLException e) {
            log.warn("[LinkExtractionUtils.extractRootDomain] Failed to extract root domain from URL: {}", url);
            return null;
        }
    }
}